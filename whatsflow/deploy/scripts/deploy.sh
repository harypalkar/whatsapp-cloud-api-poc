#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose pull || true
docker compose up -d --build
echo "WhatsFlow stack starting. API :8080, Web :4200, Nginx :80"
