package org.zerock.triplet.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.triplet.domain.card.dto.BenefitDTO;
import org.zerock.triplet.domain.card.dto.CardWithBenefitsDTO;
import org.zerock.triplet.domain.card.repository.CardRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository repo;

    public List<CardWithBenefitsDTO> listByTheme(Integer theme){
        return repo.findByTheme(theme).stream()
                .map(c -> new CardWithBenefitsDTO(
                        c.getId(),
                        c.getName(),
                        c.getIntro(),
                        c.getDesc(),
                        c.getBenefits().stream()
                                .map(b-> new BenefitDTO(
                                        b.getBenefitTitle(),
                                        b.getBenefitContent(),
                                        b.getShortTitle(),
                                        b.getShortContent()))
                                .toList()
                ))
                .toList();
    }

    public List<CardWithBenefitsDTO> cardList(){
        return repo.findAll().stream()
                .map(c -> new CardWithBenefitsDTO(
                        c.getId(),
                        c.getName(),
                        c.getIntro(),
                        c.getDesc(),
                        c.getBenefits().stream()
                                .map(b-> new BenefitDTO(
                                        b.getBenefitTitle(),
                                        b.getBenefitContent(),
                                        b.getShortTitle(),
                                        b.getShortContent()))
                                .toList()
                ))
                .toList();
    }
}
