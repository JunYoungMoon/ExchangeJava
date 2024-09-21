package com.member.exchange.repository.slave;

import com.member.exchange.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaveMemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUuid(String uuid);
    Optional<Member> findBySocialIdx(Long socialIdx);
}
