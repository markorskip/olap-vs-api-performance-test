#!/usr/bin/env bash
set -euo pipefail

PSQL_URL="${APP_POSTGRES_URL:-postgresql://benchmark:benchmark@localhost:5432/benchmark}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

psql "$PSQL_URL" -f "$ROOT_DIR/sql/postgres/01-create-schemas.sql"
psql "$PSQL_URL" -f "$ROOT_DIR/sql/postgres/02-create-tables.sql"
psql "$PSQL_URL" -f "$ROOT_DIR/sql/postgres/03-create-view.sql"
