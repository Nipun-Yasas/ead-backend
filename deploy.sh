#!/bin/bash

# Deployment script for EC2
# This script will be executed on the EC2 instance

set -e  # Exit on any error

echo "=================================================="
echo "🚀 Starting Deployment Process"
echo "=================================================="

# Configuration
ECR_REGISTRY="351889158954.dkr.ecr.eu-north-1.amazonaws.com"
ECR_REPOSITORY="ead-backend"
IMAGE_TAG="${1:-latest}"  # Use parameter or default to 'latest'
CONTAINER_NAME="ead-backend-app"
APP_PORT="8090"

# AWS Region (change if needed)
AWS_REGION="eu-north-1"

echo "📦 Image: ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"
echo "🐳 Container: ${CONTAINER_NAME}"
echo ""

# Step 1: Login to ECR
echo "🔐 Step 1: Logging into AWS ECR..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
echo "✅ ECR login successful"
echo ""

# Step 2: Pull the latest image
echo "⬇️  Step 2: Pulling Docker image from ECR..."
docker pull ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
echo "✅ Image pulled successfully"
echo ""

# Step 3: Stop and remove old container (if exists)
echo "🛑 Step 3: Stopping old container (if running)..."
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    docker stop ${CONTAINER_NAME} || true
    docker rm ${CONTAINER_NAME} || true
    echo "✅ Old container removed"
else
    echo "ℹ️  No existing container found"
fi
echo ""

# Step 4: Start new container
echo "🚀 Step 4: Starting new container..."
docker run -d \
    --name ${CONTAINER_NAME} \
    --restart unless-stopped \
    -p ${APP_PORT}:${APP_PORT} \
    -e DATASOURCE_URL="${DATASOURCE_URL}" \
    -e DATASOURCE_USERNAME="${DATASOURCE_USERNAME}" \
    -e DATASOURCE_PASSWORD="${DATASOURCE_PASSWORD}" \
    -e JWT_SECRET="${JWT_SECRET}" \
    -e JWT_EXPIRATION="${JWT_EXPIRATION}" \
    -e SERVER_PORT="${SERVER_PORT}" \
    -e FRONTEND_URL="${FRONTEND_URL}" \
    -e GEMINI_API_KEY="${GEMINI_API_KEY}" \
    -e MAIL_MAILER="${MAIL_MAILER}" \
    -e MAIL_HOST="${MAIL_HOST}" \
    -e MAIL_PORT="${MAIL_PORT}" \
    -e MAIL_USERNAME="${MAIL_USERNAME}" \
    -e MAIL_PASSWORD="${MAIL_PASSWORD}" \
    -e MAIL_ENCRYPTION="${MAIL_ENCRYPTION}" \
    -e MAIL_FROM_ADDRESS="${MAIL_FROM_ADDRESS}" \
    -e MAIL_FROM_NAME="${MAIL_FROM_NAME}" \
    ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}

echo "✅ Container started"
echo ""

# Step 5: Wait for application to start
echo "⏳ Step 5: Waiting for application to start (60 seconds)..."
sleep 60
echo ""

# Step 6: Health checks
echo "🏥 Step 6: Running health checks..."
echo ""

# Check if container is running
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "  ✅ Container is running"
else
    echo "  ❌ Container failed to start!"
    echo "  📋 Last 50 lines of logs:"
    docker logs --tail 50 ${CONTAINER_NAME}
    exit 1
fi

# Check for errors in logs
if docker logs ${CONTAINER_NAME} 2>&1 | grep -q "Started BackendApplication"; then
    echo "  ✅ Spring Boot application started successfully"
    STARTUP_TIME=$(docker logs ${CONTAINER_NAME} 2>&1 | grep "Started BackendApplication" | grep -oE '[0-9]+\.[0-9]+ seconds' || echo "unknown")
    echo "  ⏱️  Startup time: ${STARTUP_TIME}"
else
    echo "  ⚠️  Application may not have started completely"
    echo "  📋 Last 30 lines of logs:"
    docker logs --tail 30 ${CONTAINER_NAME}
fi

echo ""
echo "=================================================="
echo "✅ Deployment Complete!"
echo "=================================================="
echo ""
echo "📊 Container Status:"
docker ps --filter name=${CONTAINER_NAME} --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""
echo "📋 Recent Logs:"
docker logs --tail 20 ${CONTAINER_NAME}
echo ""
echo "=================================================="
