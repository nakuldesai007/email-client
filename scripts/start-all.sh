#!/bin/zsh

set -euo pipefail

echo "🚀 Starting Email Client"
echo "========================"
echo ""

# Check if BW_SESSION is set
if [[ -z "${BW_SESSION:-}" ]]; then
  echo "❌ Error: BW_SESSION is not set."
  echo ""
  echo "Please unlock Bitwarden first:"
  echo "  export BW_SESSION=\$(bw unlock --raw)"
  echo ""
  exit 1
fi

# Load environment variables from Bitwarden
echo "📦 Step 1: Loading credentials from Bitwarden..."
source "$(dirname "$0")/load-email-env.sh"

# Verify required environment variables are set (only email credentials required)
required_vars=(
  "EMAIL_CLIENT_IMAP_USER"
  "EMAIL_CLIENT_IMAP_PASSWORD"
  "EMAIL_CLIENT_SMTP_USER"
  "EMAIL_CLIENT_SMTP_PASSWORD"
)

missing_vars=()
for var in "${required_vars[@]}"; do
  # Use eval for POSIX compatibility (works in both zsh and bash/sh)
  value=$(eval echo "\$$var")
  if [[ -z "$value" ]]; then
    missing_vars+=("$var")
  fi
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
  echo "❌ Error: Missing required environment variables from Bitwarden:"
  printf '  - %s\n' "${missing_vars[@]}"
  echo ""
  echo "Please ensure all fields are set in your Bitwarden item 'Email Client Backend'"
  echo "Run ./scripts/setup-bitwarden.sh for help"
  exit 1
fi

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo ""
echo "🐳 Step 2: Starting PostgreSQL and Envoy proxy..."
cd "$PROJECT_ROOT"
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "   Waiting for PostgreSQL to be ready..."
for i in {1..30}; do
  if docker exec email-client-postgres-1 pg_isready -U "${POSTGRES_USER}" >/dev/null 2>&1; then
    echo "   ✓ PostgreSQL is ready"
    break
  fi
  sleep 1
  if [[ $i -eq 30 ]]; then
    echo "   ❌ PostgreSQL failed to start"
    exit 1
  fi
done

echo ""
echo "⚙️  Step 3: Starting backend..."
cd "$PROJECT_ROOT/backend"
mvn spring-boot:run > /tmp/email-backend.log 2>&1 &
BACKEND_PID=$!

echo "   Backend PID: $BACKEND_PID"
echo "   Logs: tail -f /tmp/email-backend.log"

# Wait for backend to start
echo "   Waiting for backend to start..."
for i in {1..30}; do
  if lsof -ti :9090 >/dev/null 2>&1; then
    echo "   ✓ Backend started on port 9090"
    break
  fi
  sleep 1
  if [[ $i -eq 30 ]]; then
    echo "   ❌ Backend failed to start. Check logs: tail -f /tmp/email-backend.log"
    exit 1
  fi
done

echo ""
echo "🌐 Step 4: Starting frontend..."
cd "$PROJECT_ROOT/frontend"
npm run dev > /tmp/email-frontend.log 2>&1 &
FRONTEND_PID=$!

echo "   Frontend PID: $FRONTEND_PID"
echo "   Logs: tail -f /tmp/email-frontend.log"

# Wait for frontend to start
echo "   Waiting for frontend to start..."
for i in {1..20}; do
  if lsof -ti :5173 >/dev/null 2>&1; then
    echo "   ✓ Frontend started on port 5173"
    break
  fi
  sleep 1
done

echo ""
echo "✅ All services started successfully!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📧 Email Client Ready"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "  Frontend:   http://localhost:5173"
echo "  Backend:    localhost:9090 (gRPC)"
echo "  Envoy:      localhost:8080 (gRPC-Web)"
echo "  PostgreSQL: localhost:5432 (${POSTGRES_DB})"
echo ""
echo "  Backend logs:  tail -f /tmp/email-backend.log"
echo "  Frontend logs: tail -f /tmp/email-frontend.log"
echo ""
echo "To stop all services:"
echo "  ./scripts/stop-all.sh"
echo ""

