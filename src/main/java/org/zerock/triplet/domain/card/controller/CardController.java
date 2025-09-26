package org.zerock.triplet.domain.card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.zerock.triplet.domain.card.dto.CardReqDTO;
import org.zerock.triplet.domain.card.dto.CardWithBenefitsDTO;
import org.zerock.triplet.domain.card.entity.MemberCard;
import org.zerock.triplet.domain.card.service.CardService;
import org.zerock.triplet.domain.member.entity.Member;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardController {

    private final CardService service;

    @GetMapping("/recommandCard")
    public ResponseEntity<List<CardWithBenefitsDTO>> list(@RequestParam("themeNum") Integer theme){
        return ResponseEntity.ok(service.listByTheme(theme));
    }

    @GetMapping("/list")
    public ResponseEntity<List<CardWithBenefitsDTO>> list(){
        return ResponseEntity.ok(service.cardList());
    }

    @PostMapping("/apply")
    public ResponseEntity<Long> applyCard(
            @AuthenticationPrincipal Member member,
            @RequestBody CardReqDTO req){
        MemberCard saved = service.cardApply(member, req);
        // Location 헤더까지 깔끔하게
        URI location = URI.create("/api/card/" + saved.getId());
        return ResponseEntity.created(location).body(saved.getId());
    }

}
