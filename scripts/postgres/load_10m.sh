#!/usr/bin/env bash
set -euo pipefail
CUSTOMER_COUNT="${CUSTOMER_COUNT:-1000000}" PRODUCT_COUNT="${PRODUCT_COUNT:-100000}" "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/load_dataset.sh" 10000000
