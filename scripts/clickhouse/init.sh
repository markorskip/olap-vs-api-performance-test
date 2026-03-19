#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

run_clickhouse_client() {
  if command -v clickhouse-client >/dev/null 2>&1; then
    clickhouse-client --host "${CLICKHOUSE_HOST:-localhost}" --multiquery "$@"
    return
  fi

  docker compose -f "$ROOT_DIR/docker-compose.yml" exec -T clickhouse clickhouse-client --multiquery "$@"
}

run_clickhouse_client < "$ROOT_DIR/sql/clickhouse/01-create-database.sql"
run_clickhouse_client < "$ROOT_DIR/sql/clickhouse/02-create-order-grid-flat.sql"
