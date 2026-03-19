#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <row_count>" >&2
  exit 1
fi

ROW_COUNT="$1"
PSQL_URL="${APP_POSTGRES_URL:-postgresql://benchmark:benchmark@localhost:5432/benchmark}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
CUSTOMER_COUNT="${CUSTOMER_COUNT:-100000}"
PRODUCT_COUNT="${PRODUCT_COUNT:-10000}"

psql "$PSQL_URL" -f "$ROOT_DIR/sql/postgres/04-truncate.sql"

psql "$PSQL_URL" -v customer_count="$CUSTOMER_COUNT" -v product_count="$PRODUCT_COUNT" -v row_count="$ROW_COUNT" <<'SQL'
insert into customer_domain.customers (external_id, name, region, segment, status, created_at)
select
    format('CUST-%s', gs),
    format('Customer %s', gs),
    (array['NA','EMEA','APAC','LATAM'])[1 + ((gs - 1) % 4)],
    (array['Enterprise','MidMarket','SMB','Public'])[1 + ((gs - 1) % 4)],
    (array['ACTIVE','INACTIVE'])[1 + ((gs - 1) % 2)],
    now() - ((gs % 365) || ' days')::interval
from generate_series(1, :customer_count) gs;

insert into product_domain.products (sku, name, category, status, list_price, created_at)
select
    format('SKU-%s', gs),
    format('Product %s', gs),
    (array['Hardware','Software','Services','Subscriptions','Support'])[1 + ((gs - 1) % 5)],
    (array['ACTIVE','RETIRED'])[1 + ((gs - 1) % 2)],
    round((10 + ((gs % 20000) / 10.0))::numeric, 2),
    now() - ((gs % 365) || ' days')::interval
from generate_series(1, :product_count) gs;

insert into order_domain.orders (customer_id, product_id, quantity, unit_price, order_status, sales_channel, ordered_at)
select
    1 + ((gs - 1) % :customer_count),
    1 + ((gs - 1) % :product_count),
    1 + ((gs - 1) % 10),
    round((15 + ((gs % 10000) / 7.0))::numeric, 2),
    (array['NEW','PROCESSING','SHIPPED','CANCELLED'])[1 + ((gs - 1) % 4)],
    (array['WEB','PARTNER','DIRECT','MARKETPLACE'])[1 + ((gs - 1) % 4)],
    now() - ((gs % 3650) || ' minutes')::interval
from generate_series(1, :row_count) gs;
SQL

psql "$PSQL_URL" -f "$ROOT_DIR/sql/postgres/03-create-view.sql"
