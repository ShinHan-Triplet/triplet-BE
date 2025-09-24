package org.zerock.triplet.domain.gather.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.triplet.domain.gather.dto.GatherResponse;
import org.zerock.triplet.domain.gather.dto.MyCardListResponse;
import org.zerock.triplet.domain.gather.service.GatherService;

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


}
