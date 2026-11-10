#!/usr/bin/env bash
# ==============================================================================
# HRMS Portal — Automated Linux VPS Deployment Script
# Usage: sudo ./deploy.sh
# ==============================================================================
set -e

APP_DIR="/opt/hrms-portal"
SERVICE_NAME="hrms.service"
JAR_NAME="hrms-portal.jar"

echo "======================================================"
echo " Starting HRMS Portal Deployment"
echo "======================================================"

# 1. Ensure application directory exists
mkdir -p "$APP_DIR"
mkdir -p "$APP_DIR/uploads"

# 2. Build executable JAR
echo "[1/4] Building JAR with Maven..."
mvn clean package -DskipTests

# 3. Stop running service if active
if systemctl is-active --quiet "$SERVICE_NAME"; then
    echo "[2/4] Stopping existing $SERVICE_NAME..."
    systemctl stop "$SERVICE_NAME"
fi

# 4. Copy new build artifacts
echo "[3/4] Deploying JAR to $APP_DIR/$JAR_NAME..."
cp target/hrms-portal-*.jar "$APP_DIR/$JAR_NAME"

# Ensure non-root ownership if user exists
if id "hrms" &>/dev/null; then
    chown -R hrms:hrms "$APP_DIR"
fi

# 5. Start / Restart Service
echo "[4/4] Starting $SERVICE_NAME..."
systemctl daemon-reload
systemctl enable "$SERVICE_NAME"
systemctl start "$SERVICE_NAME"

# Check health status
sleep 5
if systemctl is-active --quiet "$SERVICE_NAME"; then
    echo "======================================================"
    echo " SUCCESS: HRMS Portal is running!"
    echo " Status: $(systemctl is-active $SERVICE_NAME)"
    echo " Check logs with: journalctl -u $SERVICE_NAME -f"
    echo "======================================================"
else
    echo " ERROR: Service failed to start. Showing logs:"
    journalctl -u "$SERVICE_NAME" -n 25 --no-pager
    exit 1
fi
