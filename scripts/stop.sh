#!/bin/bash
set -e
cd /home/ec2-user/boombim || exit 0

echo "[CodeDeploy] Stopping containers..."
docker compose down || docker-compose down || true