package org.zerock.triplet.domain.card.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.triplet.domain.card.dto.CardWithBenefitsDTO;
import org.zerock.triplet.domain.card.service.CardService;

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
}
