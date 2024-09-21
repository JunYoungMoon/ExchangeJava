package com.member.exchange.repository.master;

import com.member.exchange.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterMemberRepository extends JpaRepository<Member, Long> {
}
