package org.zerock.triplet.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zerock.triplet.domain.member.dto.IdName;
import org.zerock.triplet.domain.member.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthId(String oauthId);
    Optional<Member> findById(Long id);

    @Query("""
        select new org.zerock.triplet.domain.member.dto.IdName(m.id, m.name)
        from Member m
        where m.id in :ids
    """)
    List<IdName> findNamesByIds(@Param("ids")Collection<Long> ids);

}
