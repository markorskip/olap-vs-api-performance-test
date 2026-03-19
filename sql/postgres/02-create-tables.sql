create table if not exists customer_domain.customers (
    id bigint generated always as identity primary key,
    external_id text not null unique,
    name text not null,
    region text not null,
    segment text not null,
    status text not null,
    created_at timestamptz not null default now()
);

create table if not exists product_domain.products (
    id bigint generated always as identity primary key,
    sku text not null unique,
    name text not null,
    category text not null,
    status text not null,
    list_price numeric(12, 2) not null,
    created_at timestamptz not null default now()
);

create table if not exists order_domain.orders (
    id bigint generated always as identity primary key,
    customer_id bigint not null references customer_domain.customers (id),
    product_id bigint not null references product_domain.products (id),
    quantity integer not null,
    unit_price numeric(12, 2) not null,
    order_status text not null,
    sales_channel text not null,
    ordered_at timestamptz not null
);

create index if not exists idx_customer_region on customer_domain.customers (region, segment);
create index if not exists idx_product_category on product_domain.products (category, status);
create index if not exists idx_order_customer on order_domain.orders (customer_id);
create index if not exists idx_order_product on order_domain.orders (product_id);
create index if not exists idx_order_ordered_at on order_domain.orders (ordered_at desc);
create index if not exists idx_order_status_channel on order_domain.orders (order_status, sales_channel);
