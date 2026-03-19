#!/usr/bin/env bash
set -euo pipefail

CLICKHOUSE_URL="${APP_CLICKHOUSE_URL:-http://localhost:8123}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
clickhouse-client --host "${CLICKHOUSE_HOST:-localhost}" --multiquery < "$ROOT_DIR/sql/clickhouse/01-create-database.sql"
clickhouse-client --host "${CLICKHOUSE_HOST:-localhost}" --multiquery < "$ROOT_DIR/sql/clickhouse/02-create-order-grid-flat.sql"
