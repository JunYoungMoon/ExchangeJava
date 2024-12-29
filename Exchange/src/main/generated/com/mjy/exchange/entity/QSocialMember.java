package com.mjy.exchange.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSocialMember is a Querydsl query type for SocialMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialMember extends EntityPathBase<SocialMember> {

    private static final long serialVersionUID = -2127039130L;

    public static final QSocialMember socialMember = new QSocialMember("socialMember");

    public final NumberPath<Long> idx = createNumber("idx", Long.class);

    public final EnumPath<com.mjy.exchange.enums.AuthProvider> provider = createEnum("provider", com.mjy.exchange.enums.AuthProvider.class);

    public final StringPath providerId = createString("providerId");

    public QSocialMember(String variable) {
        super(SocialMember.class, forVariable(variable));
    }

    public QSocialMember(Path<? extends SocialMember> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSocialMember(PathMetadata metadata) {
        super(SocialMember.class, metadata);
    }

}

