#!/bin/zsh

set -euo pipefail

echo "🛑 Stopping Email Client Services"
echo "=================================="
echo ""

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Stop backend
echo "Stopping backend (port 9090)..."
if lsof -ti :9090 >/dev/null 2>&1; then
  lsof -ti :9090 | xargs kill -9 2>/dev/null
  echo "  ✓ Backend stopped"
else
  echo "  ℹ Backend not running"
fi

# Stop frontend
echo "Stopping frontend (port 5173)..."
if lsof -ti :5173 >/dev/null 2>&1; then
  lsof -ti :5173 | xargs kill -9 2>/dev/null
  echo "  ✓ Frontend stopped"
else
  echo "  ℹ Frontend not running"
fi

# Stop Envoy
echo "Stopping Envoy proxy..."
cd "$PROJECT_ROOT"
docker-compose down 2>/dev/null && echo "  ✓ Envoy stopped" || echo "  ℹ Envoy not running"

# Clean up log files
echo ""
echo "Cleaning up log files..."
rm -f /tmp/email-backend.log /tmp/email-frontend.log 2>/dev/null
echo "  ✓ Logs cleaned"

echo ""
echo "✅ All services stopped"
echo ""

