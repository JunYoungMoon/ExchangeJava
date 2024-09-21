package com.member.exchange.entity;

import com.member.exchange.enums.AuthProvider;
import lombok.*;

import jakarta.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    @Column(nullable = false)
    private String providerId;

    @Builder
    public SocialMember(Long idx, AuthProvider provider, String providerId) {
        this.idx = idx;
        this.provider = provider;
        this.providerId = providerId;
    }
}
