#!/usr/bin/env bash
set -euo pipefail
CUSTOMER_COUNT="${CUSTOMER_COUNT:-10000000}" PRODUCT_COUNT="${PRODUCT_COUNT:-1000000}" "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/load_dataset.sh" 100000000
