#!/usr/bin/env bash
set -euo pipefail
CUSTOMER_COUNT="${CUSTOMER_COUNT:-1000}" PRODUCT_COUNT="${PRODUCT_COUNT:-250}" "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/load_dataset.sh" 1000
