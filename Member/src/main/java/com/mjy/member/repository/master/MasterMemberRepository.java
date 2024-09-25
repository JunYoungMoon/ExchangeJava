package com.mjy.member.repository.master;

import com.mjy.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterMemberRepository extends JpaRepository<Member, Long> {
}
