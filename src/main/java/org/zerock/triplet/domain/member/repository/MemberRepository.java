package org.zerock.triplet.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthId(String oauthId);
}
