package org.zerock.triplet.domain.gather.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.gather.dto.GatherResponse;
import org.zerock.triplet.domain.gather.dto.InviteDTO;
import org.zerock.triplet.domain.gather.dto.MyCardListResponse;
import org.zerock.triplet.domain.gather.service.GatherService;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.List;

@RestController
@RequestMapping("/api/gather")
@RequiredArgsConstructor
public class GatherController {

    private final GatherService service;

    @GetMapping("/me")
    public ResponseEntity<GatherResponse> getMyOptions(
            @AuthenticationPrincipal org.zerock.triplet.domain.member.entity.Member me
    ){
        Long memberId = me.getId();
        return ResponseEntity.ok(service.getMyOptions(memberId));
    }

    @GetMapping("/myCard")
    public ResponseEntity<MyCardListResponse> getMyCards(
            @AuthenticationPrincipal org.zerock.triplet.domain.member.entity.Member me
    ){
        Long memberId = me.getId();
        return ResponseEntity.ok(service.getMyCards(memberId));
    }

    // 모임장 : 이메일로 초대
    @PostMapping("/invites/by-email")
    public ResponseEntity<Void> createInviteByEmail(
            @AuthenticationPrincipal Member me,
            @RequestBody InviteDTO.CreateByEmailRequest req
    ) {
        service.createInviteByEmail(me.getId(), req);
        return ResponseEntity.noContent().build();
    }

    // 내 대기 목록
    @GetMapping("/invites/me")
    public ResponseEntity<List<InviteDTO.Simple>> myInvites(@AuthenticationPrincipal Member me) {
        return ResponseEntity.ok(service.getMyPendingInvites(me));
    }

    // 초대 수락
    @PatchMapping("/invites/{inviteId}/accept")
    public ResponseEntity<Void> accept(@AuthenticationPrincipal Member me,
                                       @PathVariable Long inviteId) {
        service.acceptInvite(me, inviteId);
        return ResponseEntity.noContent().build();
    }

    // 초대 거절
    @PatchMapping("/invites/{inviteId}/reject")
    public ResponseEntity<Void> reject(@AuthenticationPrincipal Member me,
                                       @PathVariable Long inviteId) {
        service.rejectInvite(me, inviteId);
        return ResponseEntity.noContent().build();
    }
}
