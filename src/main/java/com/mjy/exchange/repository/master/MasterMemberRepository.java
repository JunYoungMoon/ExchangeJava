package com.mjy.exchange.repository.master;

import com.mjy.exchange.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterMemberRepository extends JpaRepository<Member, Long> {
}
