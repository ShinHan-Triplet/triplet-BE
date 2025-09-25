package org.zerock.triplet.domain.mycard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zerock.triplet.domain.card.entity.MemberCard;
import org.zerock.triplet.domain.mycard.dto.*;
import org.zerock.triplet.domain.mycard.repository.MycardRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MycardService {

    private final MycardRepository mycardRepository;

    public List<MycardListItemDTO> getMyCards(Long memberId) {
        List<MycardListItemDTO> rows = mycardRepository.findMyCardsByMemberId(memberId);
        return rows.stream().toList();
    }

    public List<BenefitDTO> getCardBenefits(Long cardId) {
        return mycardRepository.findBenefitsByMcard(cardId);
    }

    public Optional<MycardHistoryResponse> getMyCardHistory(Long memberId, Long mcardId) {
        Optional<MycardHistoryResponse> headerOpt = mycardRepository.findMyCardHeader(memberId, mcardId);
        if (headerOpt.isEmpty()) return Optional.empty();

        List<UsageItemDTO> histories = mycardRepository.findUsagesByMcardId(memberId, mcardId);

        MycardHistoryResponse header = headerOpt.get();
        header.setHistories(histories);
        return Optional.of(header);
    }

    public MycardDetailDTO getDetail(Long mcardId){
        MemberCard memberCard = mycardRepository.findMemberCardById(mcardId);
        return MycardDetailDTO.builder()
                .benefits(getCardBenefits(memberCard.getCard().getId()))
                .nickname(memberCard.getCardNickname())
                .name(memberCard.getCard().getName())
                .mcardId(mcardId)
                .cardId(memberCard.getCard().getId())
                .cardStatus(memberCard.getCardStatus())
                .build();
    }
}
