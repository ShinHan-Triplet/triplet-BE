package org.zerock.triplet.domain.mytrip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.mytrip.dto.*;
import org.zerock.triplet.domain.mytrip.repository.MytripRepository;
import org.zerock.triplet.domain.trip.repository.CostRepository;
import org.zerock.triplet.domain.trip.entity.Trip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MytripService {
    private final MytripRepository mytripRepository;
    private final CostRepository costRepository;

    public TripListResponse getMyTrips(Long memberId) {
        // 1) 접근가능한 Trip들
        List<Trip> trips = mytripRepository.findTripsICanAccess(memberId);
        if (trips.isEmpty()) {
            return TripListResponse.builder().trips(List.of()).build();
        }

        // 2) gatherId 묶기
        List<Long> gatherIds = trips.stream().map(Trip::getGatherId).distinct().toList();

        // 3) gatherName 맵
        Map<Long, String> gatherNameMap = mytripRepository.findGatherNames(gatherIds).stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> String.valueOf(r[1])
                ));

        // 4) 멤버 목록 맵 (gatherId -> List<TripMember>)
        Map<Long, List<TripMember>> membersByGather = mytripRepository
                .findMembersByGatherIds(gatherIds).stream()
                .collect(Collectors.groupingBy(
                        r -> ((Number) r[0]).longValue(),
                        Collectors.mapping(r -> new TripMember(
                                ((Number) r[1]).longValue(),
                                String.valueOf(r[2])
                        ), Collectors.toList())
                ));

        // 5) 내가 오너인 모임 집합
        Set<Long> ownerSet = new HashSet<>(mytripRepository.findOwnerGatherIds(memberId, gatherIds));

        // 6) Trip → TripListItem 변환
        List<TripListItem> items = trips.stream().map(t -> {
            Long gid = t.getGatherId();
            String gname = gatherNameMap.getOrDefault(gid, null);
            var members = membersByGather.getOrDefault(gid, List.of());

            // Trip 엔티티는 LocalDateTime, DTO는 LocalDate이므로 변환
            LocalDate s = t.getStartDate() == null ? null : t.getStartDate().toLocalDate();
            LocalDate e = t.getEndDate() == null ? null : t.getEndDate().toLocalDate();

            return TripListItem.builder()
                    .tripId(t.getId())
                    .title(t.getName())
                    .startDate(s)
                    .endDate(e)
                    .thumbnail(t.getTripImg())
                    .gatherId(gid)
                    .gatherName(gname)
                    .owner(ownerSet.contains(gid) ? 1 : 0)
                    .members(members)
                    .build();
        }).toList();

        return TripListResponse.builder()
                .trips(items)
                .build();
    }

    /** 내 여행 상세 (레포트 헤더 + 예산 요약 + 카드 + 멤버) */
    public TripDetailResponse getTripDetail(Long tripId, Long memberId) {
        // 1) 헤더 (권한 포함)
        var h = mytripRepository.findTripHeader(tripId, memberId).get();
        if (h == null) return null;

        // 2) 구성원
        var members = mytripRepository.findMembersByGatherId(h.getGatherId()).stream()
                .map(m -> TripMember.builder()
                        .memberId(m.getMemberId())
                        .name(m.getName())
                        .build())
                .collect(Collectors.toList());

        // 3) 대표 카드 (별도 1쿼리 유지)
        var cr = mytripRepository.findGatherCardByTripId(tripId).get();
        TripCard card = null;
        if (cr != null) {
            card = TripCard.builder()
                    .mcardId(cr.getMcardId())
                    .cardId(cr.getCardId())
                    .cardName(cr.getCardName())
                    .cardNickname(cr.getCardNickname())
                    .cardNum(cr.getCardNum())
                    .account(cr.getAccount())
                    .status(cr.getStatus())
                    .build();
        }

        // 4) 카테고리별 합계 산출
        // 4-1) 숙박/보험: trips에서 계획액
        var planBasics = mytripRepository.findTripPlanBasics(tripId);
        long stayPlanned      = planBasics.get().getStayCost();
        long insurancePlanned = planBasics.get().getInsuranceCost();

        // 4-2) 식비/교통/여가/기타: cost 합계 (계획=1 / 실사용=0)
        var planSum = mytripRepository.findBudgetTotal(tripId, 1).get();
        var usedSum = mytripRepository.findBudgetTotal(tripId, 0).get();

        long planFood = planSum.getFood();
        long planTrans = planSum.getTransport();
        long planLeis = planSum.getLeisure();
        long planEtc  = planSum.getEtc();

        long usedFood = usedSum.getFood();
        long usedTrans = usedSum.getTransport();
        long usedLeis  = usedSum.getLeisure();
        long usedEtc   = usedSum.getEtc();

        // 4-3) DTO가 요구하는 카테고리 BudgetItem 리스트
        var budget = List.of(
                BudgetItem.builder()
                        .categoryId(1).categoryName("숙박비")
                        .planned(stayPlanned).used(0L)
                        .build(),
                BudgetItem.builder()
                        .categoryId(2).categoryName("보험비")
                        .planned(insurancePlanned).used(0L)
                        .build(),
                BudgetItem.builder()
                        .categoryId(3).categoryName("식비")
                        .planned(planFood).used(usedFood)
                        .build(),
                BudgetItem.builder()
                        .categoryId(4).categoryName("교통비")
                        .planned(planTrans).used(usedTrans)
                        .build(),
                BudgetItem.builder()
                        .categoryId(5).categoryName("여가비")
                        .planned(planLeis).used(usedLeis)
                        .build(),
                BudgetItem.builder()
                        .categoryId(6).categoryName("기타")
                        .planned(planEtc).used(usedEtc)
                        .build()
        );

        // 5) 상태/테마 문자열
        String status = computeStatus(h.getStartDate(), h.getEndDate());
        String themeStr = mapTheme(h.getTheme());

        // 6) 응답 조립
        return TripDetailResponse.builder()
                .tripId(h.getTripId())
                .title(h.getTitle())
                .gatherName(h.getGatherName())
                .members(members)
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .theme(themeStr)
                .thumbnail(h.getThumbnail())
                .status(status)
                .card(card)
                .budget(budget)
                .build();
    }

    private String mapTheme(Integer theme) {
        if (theme == null) return null;
        return switch (theme) {
            case 1 -> "food";
            case 2 -> "activity";
            case 3 -> "healing";
            default -> "etc";
        };
    }

    private static String computeStatus(LocalDateTime start, LocalDateTime end) {
        var today = LocalDateTime.now();
        if (today.isBefore(start)) return "여행 전";
        if (today.isAfter(end)) return "여행 완료";
        return "여행 중";
    }

    public UsageDayResponse getUsagesByDate(Long tripId, LocalDate date, Long meId) {
        // 1) 권한/존재 검증
        boolean allowed = mytripRepository.existsTripMember(tripId, meId) > 0;
        if (!allowed) throw new NoSuchElementException("trip not found or not a member: " + tripId);

        // 2) mcard_id 조회 (없으면 빈 결과)
        Long mcardId = mytripRepository.findTripMcardId(tripId).orElse(null);
        if (mcardId == null) {
            return UsageDayResponse.builder()
                    .tripId(tripId)
                    .day(UsageDay.builder()
                            .date(date)
                            .dayTotalUsed(0L)
                            .items(List.of())
                            .build())
                    .build();
        }

        // 3) 날짜 범위 [00:00, +1일 00:00)
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.plusDays(1).atStartOfDay();

        // 4) 당일 사용내역 조회 (인터페이스 프로젝션으로 받고 → DTO로 매핑)
        List<UsageItem> rows = mytripRepository.findUsagesForDay(mcardId, from, to);

        List<UsageItem> items = rows.stream()
                .map(r -> UsageItem.builder()
                        .usageId(r.getUsageId())
                        .categoryId(r.getCategoryId())
                        .categoryName(r.getCategoryName())
                        .amount(r.getAmount())
                        .memo(r.getMemo())
                        .costDateTime(r.getCostDateTime())
                        .build())
                .collect(Collectors.toList());

        // 5) 합계 (둘 중 택1)
        // (A) DB에서 바로 합계:
        Long total = mytripRepository.sumUsagesForDay(mcardId, from, to);
        // (B) 자바에서 합계:
        // long total = rows.stream().mapToLong(r -> r.getAmount() == null ? 0L : r.getAmount()).sum();

        return UsageDayResponse.builder()
                .tripId(tripId)
                .day(UsageDay.builder()
                        .date(date)
                        .dayTotalUsed(total == null ? 0L : total)
                        .items(items)
                        .build())
                .build();
    }

    @Transactional
    public List<CostPlan> getCostPlan(Long tripId){
        return costRepository.findPlansByTripId(tripId);
    }
}
