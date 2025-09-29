package org.zerock.triplet.domain.mytrip.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.triplet.domain.draft.S3CoverService;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.mytrip.dto.*;
import org.zerock.triplet.domain.mytrip.service.MytripService;
import org.zerock.triplet.domain.trip.entity.Cost;
import org.zerock.triplet.domain.trip.entity.Trip;
import org.zerock.triplet.domain.trip.repository.CostRepository;
import org.zerock.triplet.domain.trip.repository.TripRepository;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mytrip")
public class MytripController {
    private final MytripService mytripService;
    private final S3CoverService s3CoverService;
    private final TripRepository tripRepository;
    private final CostRepository costRepository;

    private final S3Client s3;
    @Value("${triplet.s3.bucket}") private String bucket;

    // 내 여행 목록
    @GetMapping
    public TripListResponse getMyTrips(@AuthenticationPrincipal Member me) {
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");
        return mytripService.getMyTrips(me.getId());
    }

    // 내 여행 상세
    @GetMapping("/{tripId}")
    public ResponseEntity<TripDetailResponse> getTripDetail(
            @PathVariable Long tripId,
            @AuthenticationPrincipal Member me
    ) {
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");
        TripDetailResponse body = mytripService.getTripDetail(tripId, me.getId());
        if (body == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(body);
    }

    // 내 여행 예산계획
    @GetMapping("/{tripId}/budgets")
    public List<CostPlan> getCostPlan(
            @PathVariable Long tripId,
            @AuthenticationPrincipal Member me
    ){
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");
        return  mytripService.getCostPlan(tripId);
    }

    /** 내 여행 특정 일자 사용 내역 (네가 요구한 시그니처) */
    @GetMapping("/{tripId}/usages")
    public ResponseEntity<UsageDayResponse> getUsagesByDate(
            @PathVariable Long tripId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal Member me
    ) {
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");

        var body = mytripService.getUsagesByDate(tripId, date, me.getId().longValue());
        if (body == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(body);
    }

    // 내 여행 특정 일자 카드 상세 사용 내역
    @GetMapping("/{tripId}/history")
    public UsageDayResponse getTripUsages(
            @PathVariable Long tripId,
            @AuthenticationPrincipal Member me) {
        return mytripService.getTripUsages(tripId, me.getId());
    }

    @PostMapping("/{tripId}/cover")
    public Map<String, String> changeCover(@PathVariable Long tripId,
                                           @RequestBody PresignReq req){

        var r = s3CoverService.presignForFinal(tripId, req.getFilename(), req.getContentType());
        return Map.of("objectKey", r.objectKey(), "uploadUrl", r.uploadUrl());
    }


    private static Long nvl(Long v) { return v == null ? 0L : v; }

    @PatchMapping("/{tripId}")
    @Transactional
    public void updateTrip(
            @PathVariable Long tripId,
            @RequestBody TripUpdateReq req
    ){
        Trip trip = tripRepository.findTripById(tripId);
        if (req.getTitle() != null) trip.setName(req.getTitle());
        if (req.getTheme() != null) trip.setTheme(req.getTheme());
        if (req.getStartDate() != null) {
            trip.setStartDate(req.getStartDate().atStartOfDay());
        }
        if (req.getEndDate() != null) {
            trip.setEndDate(req.getEndDate().atTime(LocalTime.of(23,59,59)));
        }
        if (req.getCoverKey() != null && !req.getCoverKey().isBlank()) {
            String old = trip.getTripImg();
            trip.setTripImg(req.getCoverKey());

            // 이전 커버 삭제
            if (old != null && !old.equals(req.getCoverKey())) {
                try { s3.deleteObject(b -> b.bucket(bucket).key(old)); } catch (Exception ignore) {}
            }
        }

        if(req.getBudgets() != null){
            trip.setStayCost(req.getBudgets().get(0).getAmount());
            trip.setInsuranceCost(req.getBudgets().get(1).getAmount());
        }

        // --- costPlans 전체 교체 ---
        boolean datesChanged = (req.getStartDate() != null) || (req.getEndDate() != null);
        List<CostPlan> plans = req.getCostPlans();

        if (plans != null || datesChanged) {
            // 날짜 확정값 확보(Trip에 이미 반영된 값 사용)
            LocalDateTime st = trip.getStartDate();
            LocalDateTime en = trip.getEndDate();
            if (st == null || en == null) {
                throw new IllegalArgumentException("startDate/endDate must be set before cost plans.");
            }
            int totalDays = (int) (java.time.Duration
                    .between(st.toLocalDate().atStartOfDay(), en.toLocalDate().atTime(23,59,59)).toDays()) + 1;

            // 인바운드 유효성
            if (totalDays <= 0) throw new IllegalArgumentException("Invalid date range.");

            // 기존 Cost 전부 삭제
            costRepository.deleteByTripId(tripId);

            // 요청이 있으면 그걸 우선, 없으면 0원으로 채우기
            Map<Integer, CostPlan> byDay = new HashMap<>();
            if (plans != null) {
                for (CostPlan p : plans) {
                    if (p.getDay() == null) continue;
                    // 범위 밖 day는 컷
                    if (p.getDay() < 1 || p.getDay() > totalDays) continue;
                    byDay.put(p.getDay(), p);
                }
            }

            List<Cost> toSave = new ArrayList<>(totalDays);
            for (int d = 1; d <= totalDays; d++) {
                CostPlan p = byDay.get(d);

                Cost c = new Cost();
                c.setTrip(trip);
                c.setDay(d);
                c.setFood(nvl(p != null ? p.getFood() : null));
                c.setTransport(nvl(p != null ? p.getTransport() : null));
                c.setLeisure(nvl(p != null ? p.getLeisure() : null));
                c.setEtc(nvl(p != null ? p.getEtc() : null));
                c.setCheckPlan(p != null && Boolean.TRUE.equals(p.getCheckPlan()));

                toSave.add(c);
            }
            costRepository.saveAll(toSave);

        }

    }

    // 여행 삭제
    @DeleteMapping("/{tripId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable Long tripId,
                           @AuthenticationPrincipal Member me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        mytripService.deleteTrip(me.getId(), tripId);
    }

}
