#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
: "${POSTGRES_PORT:=5432}"
: "${POSTGRES_DB:=benchmark}"
: "${POSTGRES_USER:=benchmark}"
: "${POSTGRES_PASSWORD:=benchmark}"

if command -v clickhouse-client >/dev/null 2>&1; then
  : "${POSTGRES_HOST:=localhost}"
  CLICKHOUSE_CLIENT_CMD=(clickhouse-client --host "${CLICKHOUSE_HOST:-localhost}" --multiquery)
else
  : "${POSTGRES_HOST:=postgres}"
  CLICKHOUSE_CLIENT_CMD=(docker compose -f "$ROOT_DIR/docker-compose.yml" exec -T clickhouse clickhouse-client --multiquery)
fi

"${CLICKHOUSE_CLIENT_CMD[@]}" --query "
truncate table benchmark.order_grid_flat;
insert into benchmark.order_grid_flat
select *
from postgresql(
    '${POSTGRES_HOST}:${POSTGRES_PORT}',
    '${POSTGRES_DB}',
    'order_grid_view',
    '${POSTGRES_USER}',
    '${POSTGRES_PASSWORD}',
    'benchmark'
);
"
