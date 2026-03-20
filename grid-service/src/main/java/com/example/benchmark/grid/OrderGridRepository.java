package com.example.benchmark.grid;

import com.example.benchmark.common.OrderGridRow;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderGridRepository {
    private static final Map<String, String> SORT_COLUMNS = Map.ofEntries(
            Map.entry("orderId", "order_id"),
            Map.entry("customerId", "customer_id"),
            Map.entry("customerName", "customer_name"),
            Map.entry("customerRegion", "customer_region"),
            Map.entry("customerSegment", "customer_segment"),
            Map.entry("productId", "product_id"),
            Map.entry("productSku", "product_sku"),
            Map.entry("productName", "product_name"),
            Map.entry("productCategory", "product_category"),
            Map.entry("quantity", "quantity"),
            Map.entry("unitPrice", "unit_price"),
            Map.entry("extendedPrice", "extended_price"),
            Map.entry("orderStatus", "order_status"),
            Map.entry("salesChannel", "sales_channel"),
            Map.entry("orderedAt", "ordered_at")
    );

    private final JdbcClient postgresJdbcClient;
    private final JdbcClient clickHouseJdbcClient;

    public OrderGridRepository(@Qualifier("postgresJdbcClient") JdbcClient postgresJdbcClient,
                               @Qualifier("clickHouseJdbcClient") JdbcClient clickHouseJdbcClient) {
        this.postgresJdbcClient = postgresJdbcClient;
        this.clickHouseJdbcClient = clickHouseJdbcClient;
    }

    public List<OrderRecord> findAllOrders() {
        return postgresJdbcClient.sql("""
                select id, customer_id, product_id, quantity, unit_price, order_status, sales_channel, ordered_at
                from order_domain.orders
                """)
                .query((rs, rowNum) -> new OrderRecord(
                        rs.getLong("id"),
                        rs.getLong("customer_id"),
                        rs.getLong("product_id"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unit_price"),
                        rs.getString("order_status"),
                        rs.getString("sales_channel"),
                        rs.getTimestamp("ordered_at").toInstant()))
                .list();
    }

    public long countViewRows() {
        return postgresJdbcClient.sql("select count(*) from benchmark.order_grid_view")
                .query(Long.class)
                .single();
    }

    public long countClickHouseRows() {
        return clickHouseJdbcClient.sql("select count() from benchmark.order_grid_flat")
                .query(Long.class)
                .single();
    }

    public long postgresStorageBytes() {
        return postgresJdbcClient.sql("select pg_database_size(current_database())")
                .query(Long.class)
                .single();
    }

    public long clickHouseStorageBytes() {
        return clickHouseJdbcClient.sql("""
                select ifNull(sum(bytes_on_disk), 0)
                from system.parts
                where active and database = 'benchmark'
                """)
                .query(Long.class)
                .single();
    }

    public List<OrderGridRow> queryViewPage(String sortBy, String sortDirection, int size, int offset) {
        String sql = """
                select order_id, customer_id, customer_name, customer_region, customer_segment,
                       product_id, product_sku, product_name, product_category,
                       quantity, unit_price, extended_price, order_status, sales_channel, ordered_at
                from benchmark.order_grid_view
                order by %s %s, order_id asc
                limit :limit offset :offset
                """.formatted(sortColumn(sortBy), sortDirection(sortDirection));
        return postgresJdbcClient.sql(sql)
                .param("limit", size)
                .param("offset", offset)
                .query(this::mapRow)
                .list();
    }

    public List<OrderGridRow> queryClickHousePage(String sortBy, String sortDirection, int size, int offset) {
        String sql = """
                select order_id, customer_id, customer_name, customer_region, customer_segment,
                       product_id, product_sku, product_name, product_category,
                       quantity, unit_price, extended_price, order_status, sales_channel, ordered_at
                from benchmark.order_grid_flat
                order by %s %s, order_id asc
                limit :limit offset :offset
                """.formatted(sortColumn(sortBy), sortDirection(sortBy, sortDirection));
        return clickHouseJdbcClient.sql(sql)
                .param("limit", size)
                .param("offset", offset)
                .query(this::mapRow)
                .list();
    }

    private String sortColumn(String sortBy) {
        String column = SORT_COLUMNS.get(sortBy);
        if (column == null) {
            throw new IllegalArgumentException("Unsupported sort column: " + sortBy);
        }
        return column;
    }

    private String sortDirection(String sortDirection) {
        return switch (sortDirection.toLowerCase()) {
            case "asc" -> "asc";
            case "desc" -> "desc";
            default -> throw new IllegalArgumentException("Unsupported sort direction: " + sortDirection);
        };
    }

    private String sortDirection(String ignoredSortBy, String sortDirection) {
        return sortDirection(sortDirection);
    }

    private OrderGridRow mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new OrderGridRow(
                rs.getLong("order_id"),
                rs.getLong("customer_id"),
                rs.getString("customer_name"),
                rs.getString("customer_region"),
                rs.getString("customer_segment"),
                rs.getLong("product_id"),
                rs.getString("product_sku"),
                rs.getString("product_name"),
                rs.getString("product_category"),
                rs.getInt("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getBigDecimal("extended_price"),
                rs.getString("order_status"),
                rs.getString("sales_channel"),
                rs.getTimestamp("ordered_at").toInstant());
    }
}
