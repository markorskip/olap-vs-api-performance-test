# OLAP vs API Performance Test

This repository contains a Gradle multi-module Spring Boot benchmark with three microservices and three query patterns for the same joined data grid.

## Services

- `customer-service` exposes customer data from PostgreSQL schema `customer_domain`.
- `product-service` exposes product data from PostgreSQL schema `product_domain`.
- `grid-service` renders a browser-based data grid and exposes APIs to benchmark three query strategies against `order_domain.orders` joined to the other two services.

## Query patterns

### Pattern A: API join in memory
1. `grid-service` loads all orders from PostgreSQL.
2. It batch-fetches referenced customers from `customer-service`.
3. It batch-fetches referenced products from `product-service`.
4. It joins, sorts, and paginates in memory.

### Pattern B: PostgreSQL cross-schema view
1. PostgreSQL stores each domain in a separate logical schema.
2. `benchmark.order_grid_view` joins `order_domain.orders`, `customer_domain.customers`, and `product_domain.products`.
3. `grid-service` issues `ORDER BY ... LIMIT/OFFSET` directly against the view.

### Pattern C: ClickHouse analytic projection
1. ClickHouse hosts a flattened `benchmark.order_grid_flat` table.
2. A refresh script pulls data from the PostgreSQL view into ClickHouse.
3. `grid-service` runs the same data-grid query against ClickHouse.

## Project layout

- `common`: shared DTOs for pages and benchmark summaries.
- `customer-service`: Spring Boot service on port `8081`.
- `product-service`: Spring Boot service on port `8082`.
- `grid-service`: Spring Boot service on port `8080` plus a static HTML data grid.
- `sql/postgres`: schema, table, view, and reset scripts.
- `sql/clickhouse`: ClickHouse DDL and refresh SQL.
- `scripts/postgres`: PostgreSQL bootstrap and dataset loading scripts.
- `scripts/clickhouse`: ClickHouse bootstrap and refresh scripts.
- `docker-compose.yml`: local stack for PostgreSQL, ClickHouse, and all services.
- `kubernetes/benchmark-stack.yaml`: deployment skeleton with Prometheus scrape annotations and resource limits.

## Database objects

### PostgreSQL

Run the bootstrap script:

```bash
./scripts/postgres/init.sh
```

This creates:

- schemas: `customer_domain`, `product_domain`, `order_domain`, `benchmark`
- tables: `customers`, `products`, `orders`
- indexes for join/sort coverage
- view: `benchmark.order_grid_view`

### ClickHouse

Run the bootstrap script:

```bash
./scripts/clickhouse/init.sh
```

Then refresh the flattened table from PostgreSQL:

```bash
POSTGRES_HOST=localhost POSTGRES_USER=benchmark POSTGRES_PASSWORD=benchmark ./scripts/clickhouse/refresh_from_postgres.sh
```

## Dataset loading

All loaders use deterministic synthetic data so every strategy reads equivalent rows.

### Available loaders

```bash
./scripts/postgres/load_1k.sh
./scripts/postgres/load_1m.sh
./scripts/postgres/load_10m.sh
./scripts/postgres/load_100m.sh
./scripts/postgres/load_1b.sh
```

### Notes on very large loads

- `100M` and `1B` rows require substantial disk, RAM, WAL, and elapsed time.
- For large runs, tune PostgreSQL `maintenance_work_mem`, `max_wal_size`, checkpoint settings, and storage throughput.
- You can override dimension cardinality with `CUSTOMER_COUNT` and `PRODUCT_COUNT`.

## Running locally

### Start the stack

```bash
docker compose up --build -d
```

### Seed data

```bash
APP_POSTGRES_URL=postgresql://benchmark:benchmark@localhost:5432/benchmark ./scripts/postgres/load_1k.sh
POSTGRES_HOST=localhost POSTGRES_USER=benchmark POSTGRES_PASSWORD=benchmark ./scripts/clickhouse/refresh_from_postgres.sh
```

### Open the grid UI

Open <http://localhost:8080>.

### Query the API directly

```bash
curl 'http://localhost:8080/api/grid/orders?pattern=A&page=0&size=100&sortBy=customerName&sortDirection=asc'
curl 'http://localhost:8080/api/grid/orders?pattern=B&page=0&size=100&sortBy=customerName&sortDirection=asc'
curl 'http://localhost:8080/api/grid/orders?pattern=C&page=0&size=100&sortBy=customerName&sortDirection=asc'
```

### Run the benchmark endpoint

```bash
curl -X POST ./s\
  -H 'Content-Type: application/json' \
  -d '{"pattern":"B","iterations":5,"page":0,"size":100,"sortBy":"orderedAt","sortDirection":"desc"}'
```

The benchmark response includes:

- per-iteration latency in milliseconds
- JVM heap / non-heap snapshot
- pod name and namespace when running in Kubernetes

## Measuring time and memory in pods

### Application-level timing

- Every grid query returns `durationMs`.
- The benchmark endpoint aggregates `min`, `max`, and `average` query latency.

### JVM memory exposed by the service

- Responses include a `memory` snapshot from the JVM MXBean.
- Spring Boot Actuator exposes `/actuator/metrics` and `/actuator/prometheus`.

### Kubernetes pod memory

Deploy the manifests and use:

```bash
kubectl apply -f kubernetes/benchmark-stack.yaml
kubectl top pods -n olap-benchmark
kubectl port-forward -n olap-benchmark svc/grid-service 8080:8080
```

You can correlate:

- `kubectl top` for container memory in pod
- Actuator metrics such as `jvm.memory.used`
- benchmark response timings

## Suggested benchmark workflow

1. Load one dataset size.
2. Refresh ClickHouse.
3. Warm each strategy once.
4. Run the benchmark endpoint for patterns `A`, `B`, and `C` with the same `page`, `size`, and `sortBy` values.
5. Capture latency and memory metrics from API responses, `/actuator/prometheus`, and `kubectl top`.
6. Repeat for `1K`, `1M`, `10M`, `100M`, and `1B` datasets.

## Important expectations

- Pattern A is intentionally expensive and becomes unrealistic for very large datasets.
- Pattern B should benefit from PostgreSQL indexes and planner optimizations.
- Pattern C is expected to perform best for wide scans and large analytical sorts once ClickHouse is refreshed.
