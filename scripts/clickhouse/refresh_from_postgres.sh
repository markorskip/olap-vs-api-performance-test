#!/usr/bin/env bash
set -euo pipefail

: "${POSTGRES_HOST:=localhost}"
: "${POSTGRES_PORT:=5432}"
: "${POSTGRES_DB:=benchmark}"
: "${POSTGRES_USER:=benchmark}"
: "${POSTGRES_PASSWORD:=benchmark}"
: "${CLICKHOUSE_HOST:=localhost}"

clickhouse-client --host "$CLICKHOUSE_HOST" --multiquery --query "
truncate table benchmark.order_grid_flat;
insert into benchmark.order_grid_flat
select *
from postgresql(
    '${POSTGRES_HOST}:${POSTGRES_PORT}',
    '${POSTGRES_DB}',
    'benchmark.order_grid_view',
    '${POSTGRES_USER}',
    '${POSTGRES_PASSWORD}'
);
"
