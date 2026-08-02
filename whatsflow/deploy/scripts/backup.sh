#!/usr/bin/env bash
set -euo pipefail
TS=$(date +%Y%m%d_%H%M%S)
OUT=${1:-./backups}
mkdir -p "$OUT"
docker compose exec -T postgres pg_dump -U whatsflow whatsflow | gzip > "$OUT/whatsflow_$TS.sql.gz"
echo "Backup written $OUT/whatsflow_$TS.sql.gz"
