# Deployment Guide
1. Copy env secrets
2. `cd whatsflow/deploy && ./scripts/deploy.sh`
3. Apply k8s: `kubectl apply -f ../infrastructure/kubernetes`
