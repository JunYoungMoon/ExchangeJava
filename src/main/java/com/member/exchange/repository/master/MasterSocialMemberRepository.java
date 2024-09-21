package com.member.exchange.repository.master;

import com.member.exchange.entity.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterSocialMemberRepository extends JpaRepository<SocialMember, Long> {
}
