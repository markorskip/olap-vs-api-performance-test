package com.example.benchmark.product;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository {
    private final JdbcClient jdbcClient;

    public ProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ProductRecord> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbcClient.sql("""
                select id, sku, name, category, status, list_price, created_at
                from product_domain.products
                where id in (:ids)
                """)
                .param("ids", ids)
                .query((rs, rowNum) -> new ProductRecord(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getBigDecimal("list_price"),
                        rs.getTimestamp("created_at").toInstant()))
                .list();
    }
}
