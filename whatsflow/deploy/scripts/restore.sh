#!/usr/bin/env bash
set -euo pipefail
FILE=${1:?usage: restore.sh backup.sql.gz}
gunzip -c "$FILE" | docker compose exec -T postgres psql -U whatsflow whatsflow
