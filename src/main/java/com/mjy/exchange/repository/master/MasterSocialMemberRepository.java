package com.mjy.exchange.repository.master;

import com.mjy.exchange.entity.member.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterSocialMemberRepository extends JpaRepository<SocialMember, Long> {
}
