---
marp: true
html: true
title: ClickHouse for Cross-Service Analytics
description: Why API aggregation breaks down and where a ClickHouse read store fits.
theme: default
paginate: true
size: 16:9
style: |
  section {
    font-family: "Aptos", "Inter", "Helvetica Neue", Arial, sans-serif;
    color: #172033;
    background: #f7f8fb;
    padding: 54px 70px;
  }

  h1 {
    font-size: 54px;
    line-height: 1.05;
    margin: 0 0 22px;
    letter-spacing: 0;
  }

  h2 {
    font-size: 36px;
    margin: 0 0 22px;
    letter-spacing: 0;
  }

  h3 {
    font-size: 22px;
    margin: 0 0 12px;
  }

  p, li {
    font-size: 25px;
    line-height: 1.35;
  }

  ul {
    margin-top: 12px;
  }

  table {
    font-size: 21px;
    width: 100%;
  }

  th {
    background: #e7edf2;
  }

  strong {
    color: #0b7285;
  }

  .small {
    font-size: 20px;
    color: #4b5872;
  }

  .grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 34px;
    align-items: start;
  }

  .callout {
    border-left: 8px solid #0b7285;
    background: #fff;
    padding: 20px 26px;
    margin-top: 24px;
  }

  .callout p {
    margin: 0;
  }

  .diagram {
    display: flex;
    justify-content: center;
    align-items: center;
    background: #fff;
    border: 1px solid #d7dde8;
    border-radius: 8px;
    padding: 14px;
    margin: 10px 0 18px;
    height: 340px;
  }

  .diagram.compact {
    height: 265px;
  }

  .diagram img {
    display: block;
    max-width: 100%;
    max-height: 100%;
    width: auto;
    height: 100%;
    object-fit: contain;
  }

  .diagram.compact img {
    width: 100%;
    height: auto;
  }

  pre {
    background: #fff;
    border: 1px solid #d7dde8;
    border-radius: 8px;
  }
---

# ClickHouse as an Analytics Read Store

Microservices keep operational ownership local. Cross-service analytical queries need a different read path.

**Benchmark context:** order grid workload across orders, customers, and products.

---

## The Current Shape

Each service owns its data. The UI composes screens by calling service APIs.

<div class="diagram"><img src="diagrams/current-architecture.svg" alt="Traditional microservice architecture with one database per service." /></div>

This is a good transactional boundary: services can evolve independently and protect their write models.

---

## Where It Breaks

The grid needs one result set across service boundaries:

- order fields from the order service
- customer fields from the customer service
- product fields from the product service
- filtering, sorting, and paging across the combined data

<div class="diagram"><img src="diagrams/api-aggregation.svg" alt="API aggregation request sequence across order, customer, and product services." /></div>

The hard part is not joining three objects. The hard part is sorting and paging correctly when the complete ordered set lives across multiple services.

---

## The Failure Mode

Correct global sorting requires the aggregator to see the whole result set before it can return the first page.

<div class="diagram"><img src="diagrams/failure-mode.svg" alt="Failure mode where API aggregation materializes and sorts too much data in application memory." /></div>

The benchmark hit this limit quickly: API aggregation completed at `1K` and `10K`, failed at `100K`, and was skipped at `10M` and `100M`.

---

## Add a Query-Optimized Read Store

Keep service-owned operational databases. Sync the query shape into ClickHouse as a read-only projection.

<div class="diagram"><img src="diagrams/clickhouse-architecture.svg" alt="Microservice architecture with source databases synced into a ClickHouse read projection." /></div>

ClickHouse does not take ownership of writes. It serves a read model designed for the analytical query.

---

## What Changes

The UI still asks for a grid page. The read path no longer has to fan out across services per request.

<div class="diagram"><img src="diagrams/clickhouse-read-path.svg" alt="Read path where the grid API queries a ClickHouse projection instead of fanning out to services." /></div>

The join cost moves out of the request path, and the sort runs where the data layout is built for scans.

---

## Why ClickHouse Is Fast

<div class="grid">

<div>

### It is built for analytical reads

- **Columnar storage:** reads only the columns needed by the query
- **Compression:** fewer bytes move from disk to CPU
- **Vectorized execution:** processes batches efficiently
- **Data skipping:** avoids reading irrelevant parts when ordered or indexed well
- **Parallelism:** scans and aggregations use available cores

</div>

<div>

### It is not a Postgres replacement

- Postgres is the system of record for transactional writes
- Postgres is better for row-level updates, constraints, and OLTP workflows
- ClickHouse favors append-heavy analytical projections
- ClickHouse introduces refresh lag and duplicate storage
- The projection must be modeled around known query patterns

</div>

</div>

<div class="callout">
<p>Use Postgres for correctness and ownership. Use ClickHouse when the read question is large, cross-domain, and analytical.</p>
</div>

---

## Performance Results

Average latency for `POST /api/grid/orders/benchmark`, 3 iterations, `page=0`, `size=100`, `sortBy=orderedAt`, `sortDirection=desc`.

| Dataset | API Aggregation | Single Postgres | ClickHouse Projection |
| --- | ---: | ---: | ---: |
| 1K | 25.00 ms | 2.33 ms | 21.67 ms |
| 10K | 142.33 ms | 3.67 ms | 24.33 ms |
| 100K | HTTP 500 | 18.33 ms | 25.67 ms |
| 1M | HTTP 500 | 52.33 ms | 23.33 ms |
| 10M | skipped | 868.33 ms | 52.33 ms |
| 100M | skipped | 15,897.67 ms | 68.67 ms |

At `100M` rows, ClickHouse returned the grid query in about `69 ms`; Postgres took about `15.9 s`.

---

## Latency Curve

Series order: Single Postgres, ClickHouse projection.

<div class="diagram compact"><img src="diagrams/latency-curve.svg" alt="Average grid query latency chart comparing Postgres and ClickHouse." /></div>

Postgres is excellent at small sizes. ClickHouse stays flat when the workload becomes large and scan-heavy.

---

## Storage and Refresh Cost

<div class="diagram compact"><img src="diagrams/storage-comparison.svg" alt="Storage comparison chart for Postgres and ClickHouse." /></div>

| Dataset | Postgres Size | ClickHouse Size | ClickHouse Refresh |
| --- | ---: | ---: | ---: |
| 1M | 170.68 MiB | 27.74 MiB | 1.36 s |
| 10M | 1,626.60 MiB | 353.33 MiB | 16.70 s |
| 100M | 15,975.97 MiB | 4,035.30 MiB | 300.21 s |

ClickHouse was smaller at every tested size, but the projection is not free: refresh time and operational complexity are part of the design.

---

## Decision Frame

Choose the read path based on the query shape, not database preference.

- **API aggregation:** acceptable for small pages over small service-local datasets
- **Single Postgres with logical schemas:** low-friction option through moderate scale
- **ClickHouse read projection:** best fit for large, cross-service, scan-heavy analytical reads

<div class="callout">
<p>The architectural move is not replacing microservices. It is adding a read model for questions microservices are structurally bad at answering.</p>
</div>
