package com.mjy.exchange.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCoinInfo is a Querydsl query type for CoinInfo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCoinInfo extends EntityPathBase<CoinInfo> {

    private static final long serialVersionUID = 1803066206L;

    public static final QCoinInfo coinInfo = new QCoinInfo("coinInfo");

    public final StringPath coinName = createString("coinName");

    public final EnumPath<CoinInfo.CoinType> coinType = createEnum("coinType", CoinInfo.CoinType.class);

    public final NumberPath<java.math.BigDecimal> feeRate = createNumber("feeRate", java.math.BigDecimal.class);

    public final NumberPath<Long> idx = createNumber("idx", Long.class);

    public final StringPath marketName = createString("marketName");

    public QCoinInfo(String variable) {
        super(CoinInfo.class, forVariable(variable));
    }

    public QCoinInfo(Path<? extends CoinInfo> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCoinInfo(PathMetadata metadata) {
        super(CoinInfo.class, metadata);
    }

}

