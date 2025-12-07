#!/bin/bash
set -e
cd /home/ec2-user/boombim

ECR_REGISTRY=098072157131.dkr.ecr.ap-northeast-2.amazonaws.com
AWS_REGION=ap-northeast-2

echo "[deploy] Generate .env from SSM..."

PARAM_KEYS=(
  ADMIN_ID
  APPLE_CLIENT_ID
  APPLE_KEY_ID
  APPLE_PRIVATE_KEY
  APPLE_PROFILE
  APPLE_REDIRECT_URI
  APPLE_TEAM_ID
  COOKIE_FRONT_REDIRECT
  DB_PASSWORD
  DB_URL
  DB_USER
  FIREBASE_SERVICE_ACCOUNT_KEY
  GENERATE_CONGESTION_MESSAGE_API_KEY
  GENERATE_CONGESTION_MESSAGE_BASE_URL
  GENERATE_CONGESTION_MESSAGE_MAX_TOKENS
  GENERATE_CONGESTION_MESSAGE_PROMPT
  GENERATE_CONGESTION_MESSAGE_REPETITION_PENALTY
  GENERATE_CONGESTION_MESSAGE_TEMPERATURE
  GENERATE_CONGESTION_MESSAGE_TOP_K
  GENERATE_CONGESTION_MESSAGE_TOP_P
  JWT_ACCESS_EXPIRATION
  JWT_REFRESH_EXPIRATION
  JWT_SECRET
  KAKAO_CLIENT_ID
  KAKAO_CLIENT_SECRET
  KAKAO_COORDINATE_TO_REGION_CODE_API_KEY
  KAKAO_FRONT_REDIRECT_URI
  KAKAO_OAUTH2_BASE_URL
  KAKAO_REDIRECT_URI
  NAVER_CLIENT_ID
  NAVER_CLIENT_SECRET
  NAVER_OAUTH2_BASE_URL
  NAVER_REDIRECT_URI
  OPEN_API_KEY
  REDIS_HOST
  REDIS_PASSWORD
  REDIS_PORT
  S3_BASE_URL
  S3_BUCKET
  S3_PLACEHOLDER_KEY
  S3_REGION
  STATIC_MAP_API_KEY
  STATIC_MAP_API_KEY_ID
)

# .env 새로 생성
: > .env

for key in "${PARAM_KEYS[@]}"; do
  value=$(aws ssm get-parameter \
    --name "/boombim-api/${key}" \
    --with-decryption \
    --region "$AWS_REGION" \
    --query "Parameter.Value" \
    --output text)

  printf '%s=%s\n' "$key" "$value" >> .env
done

echo "[deploy] Login to ECR..."
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "[deploy] Pull latest images..."
docker compose pull || docker-compose pull

echo "[deploy] Start containers..."
docker compose up -d || docker-compose up -d

echo "[deploy] Done."
