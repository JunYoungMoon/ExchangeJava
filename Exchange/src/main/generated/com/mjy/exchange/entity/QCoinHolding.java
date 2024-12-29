package com.mjy.exchange.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCoinHolding is a Querydsl query type for CoinHolding
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoinHolding extends EntityPathBase<CoinHolding> {

    private static final long serialVersionUID = 1430782483L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCoinHolding coinHolding = new QCoinHolding("coinHolding");

    public final NumberPath<java.math.BigDecimal> availableAmount = createNumber("availableAmount", java.math.BigDecimal.class);

    public final StringPath coinType = createString("coinType");

    public final NumberPath<Long> idx = createNumber("idx", Long.class);

    public final BooleanPath isFavorited = createBoolean("isFavorited");

    public final QMember member;

    public final NumberPath<java.math.BigDecimal> usingAmount = createNumber("usingAmount", java.math.BigDecimal.class);

    public final StringPath walletAddress = createString("walletAddress");

    public QCoinHolding(String variable) {
        this(CoinHolding.class, forVariable(variable), INITS);
    }

    public QCoinHolding(Path<? extends CoinHolding> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCoinHolding(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCoinHolding(PathMetadata metadata, PathInits inits) {
        this(CoinHolding.class, metadata, inits);
    }

    public QCoinHolding(Class<? extends CoinHolding> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
    }

}

