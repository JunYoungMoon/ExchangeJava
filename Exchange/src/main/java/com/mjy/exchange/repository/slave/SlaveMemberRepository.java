package com.mjy.exchange.repository.slave;

import com.mjy.exchange.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaveMemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByUuid(String uuid);
    Optional<Member> findBySocialIdx(Long socialIdx);
    @Query("SELECT m FROM Member m JOIN FETCH m.coinHoldings WHERE m.uuid = :uuid")
    Optional<Member> findMemberWithCoinHoldingsByUuid(@Param("uuid") String uuid);
}
