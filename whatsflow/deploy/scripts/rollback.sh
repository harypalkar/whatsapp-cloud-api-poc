#!/usr/bin/env bash
set -euo pipefail
TAG=${1:?usage: rollback.sh <image-tag>}
docker compose up -d --no-deps api
echo "Rolled API to tag $TAG (set image tag in compose override as needed)"
