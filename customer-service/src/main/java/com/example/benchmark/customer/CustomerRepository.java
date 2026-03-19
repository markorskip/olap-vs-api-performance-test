package com.example.benchmark.customer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class CustomerRepository {
    private final JdbcClient jdbcClient;

    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<CustomerRecord> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("""
                select id, external_id, name, region, segment, status, created_at
                from customer_domain.customers
                where id in (:ids)
                """)
                .param("ids", ids)
                .query((rs, rowNum) -> new CustomerRecord(
                        rs.getLong("id"),
                        rs.getString("external_id"),
                        rs.getString("name"),
                        rs.getString("region"),
                        rs.getString("segment"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toInstant()))
                .list();
    }
}
