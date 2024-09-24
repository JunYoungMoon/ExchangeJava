package com.mjy.exchange.entity.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 50)
    private String uuid;

    @Column(nullable = false, length = 200)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Builder
    public RefreshToken(Long idx, String uuid, String token, Instant expiryDate) {
        this.idx = idx;
        this.uuid = uuid;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
