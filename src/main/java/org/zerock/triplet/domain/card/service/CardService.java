package org.zerock.triplet.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.zerock.triplet.domain.card.dto.BenefitDTO;
import org.zerock.triplet.domain.card.dto.CardReqDTO;
import org.zerock.triplet.domain.card.dto.CardWithBenefitsDTO;
import org.zerock.triplet.domain.card.entity.MemberCard;
import org.zerock.triplet.domain.card.repository.CardRepository;
import org.zerock.triplet.domain.card.repository.MemberCardRepository;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository repo;
    private final MemberCardRepository memberCardRepo;
    private static final int MAX_RETRIES = 8;

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

    public MemberCard cardApply(Member member, CardReqDTO req){
        MemberCard mc = new MemberCard();
        mc.setMember(member);
        mc.setCard(repo.findCardById(req.getCardId()));

        String cardNum = randomByTemplate(); // 예: "1234-56**-****-5678"
        int tries = 0;
        while (memberCardRepo.existsByCardNum(cardNum)) {
            if (++tries > MAX_RETRIES) throw new IllegalStateException("Failed to create unique card number");
            cardNum = randomByTemplate();
        }
        mc.setCardNum(cardNum);
        mc.setPw(req.getPw());
        mc.setCardNickname(req.getNickName());
        mc.setCardStatus(1);
        mc.setAccount(req.getAccount());
        mc.setCheckGather(req.isCheckGather());
        mc.setMemberNum(req.getMemberNum());
        mc.setAddress(req.getAddress());
        mc.setPhone(req.getPhone());

        memberCardRepo.save(mc);
        return mc;
    }

    private String randomByTemplate() {
        // 예시: 4-2-4-4 형식(가운데 일부 마스킹)
        ThreadLocalRandom r = ThreadLocalRandom.current();
        String p1 = String.format("%04d", r.nextInt(0, 10000));
        String p2 = String.format("%02d", r.nextInt(0, 100));
        String p3 = String.format("%04d", r.nextInt(0, 10000));
        String p4 = String.format("%04d", r.nextInt(0, 10000));
        return p1 + "-" + p2 + "**-****-" + p4;
    }

    // 카드 분실 신고
    public void reportLost(Long cardId, Long memberId) {
        int updated = memberCardRepo.updateCardStatus(cardId, memberId, 2);
        if (updated == 0) {
            throw new IllegalArgumentException("해당 카드가 없거나 권한이 없습니다.");
        }
    }

    // 카드 정지 해제
    public void resume(Long cardId, Long memberId) {
        int updated = memberCardRepo.updateCardStatus(cardId, memberId, 1);
        if (updated == 0) {
            throw new IllegalArgumentException("해당 카드가 없거나 권한이 없습니다.");
        }
    }
}
