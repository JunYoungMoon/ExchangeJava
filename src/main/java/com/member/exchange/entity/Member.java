package com.member.exchange.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "member",
        indexes = @Index(name = "idx_uuid", columnList = "uuid")
)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;
    @Column(length = 30)
    private Long socialIdx;
    @Column(nullable = false, length = 50)
    private String uuid;
    @Column(nullable = false, length = 50)
    private String email;
    @Column(nullable = false, length = 20)
    private String name;
    @Column(length = 100)
    private String password;
    @Column(length = 200)
    private String profileImage;
    @Column(length = 20)
    private String nickname;
    @Column
    private LocalDateTime registeredAt;

    @Builder
    public Member(Long idx, Long socialIdx, String uuid, String email, String name, String password, List<String> roles, String profileImage, String nickname) {
        this.idx = idx;
        this.socialIdx = socialIdx;
        this.uuid = uuid;
        this.email = email;
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.profileImage = profileImage;
        this.nickname = nickname;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
