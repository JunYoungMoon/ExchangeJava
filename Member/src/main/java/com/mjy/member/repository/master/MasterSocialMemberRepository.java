package com.mjy.member.repository.master;

import com.mjy.member.entity.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasterSocialMemberRepository extends JpaRepository<SocialMember, Long> {
}
