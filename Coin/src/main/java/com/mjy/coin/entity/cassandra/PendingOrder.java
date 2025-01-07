package com.mjy.coin.entity.cassandra;

import com.mjy.coin.enums.OrderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("pending_orders")
@Getter
@Setter
public class PendingOrder {

    @PrimaryKey
    private String orderUuid;

    @Column("coin_name")
    private String coinName;

    @Column("market_name")
    private String marketName;

    @Column("coin_amount")
    private BigDecimal coinAmount;

    @Column("order_price")
    private BigDecimal orderPrice;

    @Column("order_type")
    private OrderType orderType;

    @Column("fee")
    private BigDecimal fee;

    @Column("member_idx")
    private Long memberIdx;

    @Column("member_uuid")
    private String memberUuid;

    @Column("created_at")
    private LocalDateTime createdAt;
}