create or replace view benchmark.order_grid_view as
select
    o.id as order_id,
    c.id as customer_id,
    c.name as customer_name,
    c.region as customer_region,
    c.segment as customer_segment,
    p.id as product_id,
    p.sku as product_sku,
    p.name as product_name,
    p.category as product_category,
    o.quantity,
    o.unit_price,
    (o.unit_price * o.quantity) as extended_price,
    o.order_status,
    o.sales_channel,
    o.ordered_at
from order_domain.orders o
join customer_domain.customers c on c.id = o.customer_id
join product_domain.products p on p.id = o.product_id;
