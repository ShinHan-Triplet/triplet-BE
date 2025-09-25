package org.zerock.triplet.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
