truncate table benchmark.order_grid_flat;

insert into benchmark.order_grid_flat
select *
from postgresql(
    '{POSTGRES_HOST:postgres}:5432',
    'benchmark',
    'benchmark.order_grid_view',
    '{POSTGRES_USER:benchmark}',
    '{POSTGRES_PASSWORD:benchmark}'
);
