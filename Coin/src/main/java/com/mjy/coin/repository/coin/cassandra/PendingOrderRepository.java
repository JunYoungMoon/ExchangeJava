package com.mjy.coin.repository.coin.cassandra;

import com.mjy.coin.entity.cassandra.PendingOrder;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface PendingOrderRepository extends CassandraRepository<PendingOrder, String> {
}
