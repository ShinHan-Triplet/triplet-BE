package org.zerock.triplet.domain.mycard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.triplet.domain.member.entity.Member;
import org.zerock.triplet.domain.mycard.dto.*;
import org.zerock.triplet.domain.mycard.service.MycardService;

import java.util.List;

@RestController
@RequestMapping("/api/mycard")
@RequiredArgsConstructor
public class MycardController {

    private final MycardService mycardService;

    // 내 카드 목록
    @GetMapping
    public List<MycardListItemDTO> getMyCards(@AuthenticationPrincipal Member me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return mycardService.getMyCards(me.getId());
    }

    // 카드 혜택
    @GetMapping("/{mcardId}")
    public MycardDetailDTO getCardBenefits(@PathVariable Long mcardId) {
        return mycardService.getDetail(mcardId);
    }

    // 사용 내역
    @GetMapping("/{mcardId}/history")
    public MycardHistoryResponse getMyCardHistory(@PathVariable Long mcardId,
                                                  @AuthenticationPrincipal Member me) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return mycardService.getMyCardHistory(me.getId(), mcardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "card not found"));
    }

    // 사용 내역 수정
    @PatchMapping("/{mcardId}/history/{usageId}")
    public void updateUsage(@PathVariable Long mcardId,
                            @PathVariable Long usageId,
                            @AuthenticationPrincipal Member me,
                            @RequestBody UsageUpdateRequest req) {
        if (me == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        mycardService.updateUsage(me.getId(), mcardId, usageId, req);
    }

}
