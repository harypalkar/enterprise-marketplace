#!/usr/bin/env bash
# Run a specific microservice
# Usage: ./run-service.sh product-service
set -euo pipefail

SERVICE="${1:?Service name required, e.g. product-service}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$ROOT_DIR"
mvn spring-boot:run -pl "$SERVICE" -am -Dspring-boot.run.profiles=local
