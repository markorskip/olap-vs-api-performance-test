create table if not exists benchmark.order_grid_flat (
    order_id UInt64,
    customer_id UInt64,
    customer_name String,
    customer_region String,
    customer_segment String,
    product_id UInt64,
    product_sku String,
    product_name String,
    product_category String,
    quantity UInt32,
    unit_price Decimal(12, 2),
    extended_price Decimal(18, 2),
    order_status String,
    sales_channel String,
    ordered_at DateTime64(3)
)
engine = MergeTree
order by (ordered_at, order_id, customer_id, product_id);
