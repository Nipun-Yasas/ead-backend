#!/bin/bash

# Deployment script for EC2
# This script builds Docker image on EC2 and runs the container

set -e  # Exit on any error

echo "=================================================="
echo "🚀 Starting Deployment Process"
echo "=================================================="

# Configuration
IMAGE_NAME="ead-backend"
IMAGE_TAG="${1:-latest}"  # Use parameter or default to 'latest'
CONTAINER_NAME="ead-backend-app"
APP_PORT="8090"
BUILD_DIR="/home/ubuntu/app-deployment"

echo "📦 Image: ${IMAGE_NAME}:${IMAGE_TAG}"
echo "🐳 Container: ${CONTAINER_NAME}"
echo "📁 Build Directory: ${BUILD_DIR}"
echo ""

# Step 1: Build Docker image on EC2 (native AMD64)
echo "🔨 Step 1: Building Docker image on EC2..."
cd ${BUILD_DIR}
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
echo "✅ Docker image built successfully"
echo ""

# Step 2: Stop and remove old container (if exists)
echo "🛑 Step 2: Stopping old container (if running)..."
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    docker stop ${CONTAINER_NAME} || true
    docker rm ${CONTAINER_NAME} || true
    echo "✅ Old container removed"
else
    echo "ℹ️  No existing container found"
fi
echo ""

# Step 3: Start new container
echo "🚀 Step 3: Starting new container..."
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
    ${IMAGE_NAME}:${IMAGE_TAG}

echo "✅ Container started"
echo ""

# Step 4: Wait for application to start
echo "⏳ Step 4: Waiting for application to start (60 seconds)..."
sleep 60
echo ""

# Step 5: Health checks
echo "🏥 Step 5: Running health checks..."
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
