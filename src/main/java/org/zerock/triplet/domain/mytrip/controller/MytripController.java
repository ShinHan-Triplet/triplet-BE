package org.zerock.triplet.domain.mytrip.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.mytrip.dto.*;
import org.zerock.triplet.domain.mytrip.service.MytripService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mytrip")
public class MytripController {
    private final MytripService mytripService;

    /** 내 여행 목록 (멤버/모임 정보 포함) */
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
}
