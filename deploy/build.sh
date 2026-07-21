#!/usr/bin/env bash
# Build the backend jar and stage it as app.jar for the systemd unit.
# Run this from the repo root: ./deploy/build.sh
set -euo pipefail

cd "$(dirname "$0")/.."

mvn -q -DskipTests clean package

jar=$(ls target/cemis-backend-*.jar | grep -v sources | head -n1)
cp "$jar" /opt/cemis/backend/app.jar

echo "Deployed $jar -> /opt/cemis/backend/app.jar"
echo "Restart the service with: sudo systemctl restart cemis-backend"
