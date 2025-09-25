package org.zerock.triplet.domain.trip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.triplet.domain.draft.Draft;
import org.zerock.triplet.domain.draft.DraftService;
import org.zerock.triplet.domain.draft.S3CoverService;
import org.zerock.triplet.domain.gather.entity.Gather;
import org.zerock.triplet.domain.gather.entity.GatherMapping;
import org.zerock.triplet.domain.gather.repository.GatherMappingRepository;
import org.zerock.triplet.domain.gather.repository.GatherRepository;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.card.repository.MemberCardRepository;
import org.zerock.triplet.domain.trip.dto.PublishReq;
import org.zerock.triplet.domain.trip.dto.PublishResult;
import org.zerock.triplet.domain.trip.entity.Cost;
import org.zerock.triplet.domain.trip.entity.Trip;
import org.zerock.triplet.domain.trip.repository.CostRepository;
import org.zerock.triplet.domain.trip.repository.TripRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TripPublishService {

    private final DraftService drafts;   //Redis 초안
    private final S3CoverService s3;     // S3 tmp -> final
    private final TripRepository trips;
    private final GatherRepository gathers;
    private final GatherMappingRepository mappings;
    private final MemberCardRepository membercardRepo;
    private final CostRepository costs;

    @Transactional
    public PublishResult publish(Member me, PublishReq req){
        Long memberId = me.getId();
        Draft draft = drafts.get(memberId)
                .orElseThrow(() -> new IllegalStateException("초안이 없습니다."));

        // 1. 모임 결정 (기존 연결 or 새롭게 생성)
        Gather gather = resolveGather(me, req);

        // 2. trip 저장
        Trip trip = toTripEntityFromDraft(draft, gather);
        trips.save(trip);

        // 3. 대표사진 s3 승격
        if(draft.getCover() != null && draft.getCover().getObjectKey() != null){
            String finalKey = s3.promoteToFinal(trip.getId(), draft.getCover().getObjectKey());
            trip.setTripImg(finalKey);
            trips.save(trip);
        }

        // 4. 일자별 비용 저장
        persistCostFromDraft(draft, trip);

        // 5. 초안 제거
        drafts.delete(memberId);

        return new PublishResult(trip.getId(), gather.getId());
    }

    // 기존모임 연결 또는 새 모임 생성
    private Gather resolveGather(Member me, PublishReq req){
        if(req.getGatherId() != null && req.getNewGather() != null){
            throw new IllegalArgumentException("GatherId 또는 newGather중 하나만 전송하세요");
        }
        if(req.getGatherId() != null){
            Gather g = gathers.findById(req.getGatherId())
                    .orElseThrow(() -> new IllegalArgumentException("모임을 찾을 수 없습니다."));
            boolean isMember = mappings.existsByGatherAndMember(g,me);
            if(!isMember) throw new SecurityException("해당 모임의 구성원이 아닙니다");
            return g;
        }
        if(req.getNewGather() != null){
            var ng = req.getNewGather();
            if(ng.getMcardId() == null) throw new IllegalArgumentException("newGather.mcardId가 필요합니다.");

            Gather g = new Gather();
            g.setName(Objects.requireNonNullElse(ng.getName(), "새 모임"));
            g.setMcard(membercardRepo.findMemberCardById(ng.getMcardId()));
            gathers.save(g);

            GatherMapping gm = new GatherMapping();
            gm.setGather(g);
            gm.setMember(me);
            gm.setCheckOwner(true);
            mappings.save(gm);
            return g;
        }
        throw new IllegalArgumentException("gatherId 또는 newGather를 제공하세요");
    }

    // 초안 -> trip엔티티 변환
    private Trip toTripEntityFromDraft(Draft d, Gather gather){
        Trip t = new Trip();
        t.setName(Objects.requireNonNullElse(d.getTitle(), "새 여행"));
        t.setGatherId(gather.getId());
        t.setTheme(d.getThemeNum());

        LocalDate start = toLocalDate(d.getStartMs());
        LocalDate end = toLocalDate(d.getEndMs());
        t.setStartDate(start.atStartOfDay());
        t.setEndDate(end.atStartOfDay());

        Long stay = Long.parseLong(d.getBudgets().getStay());
        Long ins = Long.parseLong(d.getBudgets().getInsurance());
        int daysSum = sumDaySnapshots(d);

        t.setStayCost(stay);
        t.setInsuranceCost(ins);
        t.setTotalCost(stay + ins + daysSum);
        t.setTripImg(d.getCover().getViewUrl());
        return t;
    }

    private void persistCostFromDraft(Draft d, Trip trip){
        if(d.getBudgets() == null || d.getBudgets().getDays()==null) return;
        d.getBudgets().getDays().forEach((k, snap) -> {
            int day = k;
            var a = snap.getAmounts();
            Cost c = new Cost();
            c.setTrip(trip);
            c.setDay(day);
            c.setFood(      Long.parseLong(a!=null ? a.get("food")      : "0") );
            c.setTransport( Long.parseLong(a!=null ? a.get("transport") : "0") );
            c.setLeisure(   Long.parseLong(a!=null ? a.get("leisure")   : "0") );
            c.setEtc(       Long.parseLong(a!=null ? a.get("etc")       : "0") );
            c.setCheckPlan(Boolean.TRUE.equals(snap.getNoSchedule()));
            costs.save(c);
        });
    }

    private static LocalDate toLocalDate(Long ms) {
        if (ms == null) return LocalDate.now();
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String safe(java.util.concurrent.Callable<String> c) {
        try { return c.call(); } catch (Exception e) { return null; }
    }

    private static int sumDaySnapshots(Draft d) {
        if (d.getBudgets()==null || d.getBudgets().getDays()==null) return 0;
        return d.getBudgets().getDays().values().stream().mapToInt(x -> {
            Map<String,String> a = x.getAmounts();
            return val(a,"food") + val(a,"transport") + val(a,"leisure") + val(a,"etc");
        }).sum();
    }

    private static int val(Map<String,String> a, String k) {
        try {
            if (a==null) return 0;
            String v = a.get(k);
            return (v==null || v.isBlank()) ? 0 : Integer.parseInt(v);
        } catch (Exception e) { return 0; }
    }
}
