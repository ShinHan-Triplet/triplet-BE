package org.zerock.triplet.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.mypage.dto.MypageDTO;
import org.zerock.triplet.domain.mypage.service.MypageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MypageController {
    private final MypageService mypageService;

    @GetMapping
    public MypageDTO.MyInfoResponse getMyInfo(@AuthenticationPrincipal Member me) {
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");
        return mypageService.getMyInfo(me.getId());
    }

    @GetMapping("/gather")
    public List<MypageDTO.MyGatherSummary> getMyGathers(@AuthenticationPrincipal Member me) {
        if (me == null) throw new IllegalStateException("로그인이 필요합니다.");
        return mypageService.getMyGathers(me.getId());
    }

    // 모임 삭제
    @DeleteMapping("/gather/{gatherId}")
    public ResponseEntity<Void> deleteGather(
            @PathVariable Long gatherId,
            @AuthenticationPrincipal Member me
    ) {
        mypageService.deleteGatherForOwner(gatherId, me.getId());
        return ResponseEntity.noContent().build(); // 204
    }

    // 모임 탈퇴
    @DeleteMapping("/gather/{gatherId}/leave")
    public ResponseEntity<Void> leaveGather(
            @PathVariable Long gatherId,
            @AuthenticationPrincipal Member me
    ) {
        mypageService.leaveGather(gatherId, me.getId());
        return ResponseEntity.noContent().build(); // 204
    }
    
    // 에러 핸들러
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handle403(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handle400(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
