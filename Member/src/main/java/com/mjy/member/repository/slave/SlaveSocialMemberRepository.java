package com.mjy.member.repository.slave;

import com.mjy.member.entity.SocialMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaveSocialMemberRepository extends JpaRepository<SocialMember, Long> {
    Optional<SocialMember> findByProviderId(String id);
}
