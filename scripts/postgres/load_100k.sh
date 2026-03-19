#!/usr/bin/env bash
set -euo pipefail
CUSTOMER_COUNT="${CUSTOMER_COUNT:-100000}" PRODUCT_COUNT="${PRODUCT_COUNT:-25000}" "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/load_dataset.sh" 100000
