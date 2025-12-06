#!/bin/bash
set -e
cd /home/ec2-user/boombim

ECR_REGISTRY=098072157131.dkr.ecr.ap-northeast-2.amazonaws.com
ECR_REPOSITORY=boombim-api
AWS_REGION=ap-northeast-2

echo "[deploy] Login to ECR..."
aws ecr get-login-password --region $AWS_REGION \
  | docker login --username AWS --password-stdin $ECR_REGISTRY

echo "[deploy] Pull latest images..."
docker compose pull || docker-compose pull

echo "[deploy] Start containers..."
docker compose up -d || docker-compose up -d

echo "[deploy] Done."
