# Email Client

A modern email client with a React frontend and Spring Boot gRPC backend, featuring encrypted storage and Gmail integration.

## Architecture

- **Backend**: Spring Boot with gRPC services (port 9090)
  - IMAP sync from Gmail
  - SMTP sending via Gmail
  - PostgreSQL database for email cache
  - On-demand email fetching
  - Optimized for Gmail rate limits

- **Database**: PostgreSQL 16 (port 5432)
  - Encrypted email storage
  - Fast indexed queries
  - Managed via Docker

- **Frontend**: React + TypeScript + Vite (port 5173)
  - gRPC-Web client
  - Tailwind CSS styling
  - React Query for state management
  - Beautiful email detail view

- **Proxy**: Envoy (port 8080)
  - Bridges gRPC-Web (browser) to gRPC (backend)

## Prerequisites

- Java 21+
- Node.js 18+
- Maven 3.9+
- Docker (for Envoy proxy)
- Bitwarden CLI (for secrets management)

## Setup

### 1. Configure Gmail

1. Enable IMAP in Gmail Settings
2. Create a Google App Password (Security → 2-Step Verification → App Passwords)

### 2. Store Gmail Credentials in Bitwarden

Create a Bitwarden item named "Email Client Backend" with **only 4 custom fields**:

| Field Name                | Value                                      | Description                        |
|---------------------------|--------------------------------------------|------------------------------------|
| `IMAP_USER`               | your_email@gmail.com                       | Gmail email address                |
| `IMAP_PASSWORD`           | Your Google app password                   | Gmail app-specific password        |
| `SMTP_USER`               | your_email@gmail.com                       | Gmail email address                |
| `SMTP_PASSWORD`           | Your Google app password                   | Gmail app-specific password        |

**That's it!** All other configuration (PostgreSQL, crypto keys, server settings) has built-in defaults.

### 3. Start All Services

**IMPORTANT:** All credentials must be loaded from Bitwarden. The application will not start without them.

**Option A: Use the automated startup script (recommended)**
```bash
# Unlock Bitwarden
export BW_SESSION=$(bw unlock --raw)

# Start all services (backend, envoy, frontend)
./scripts/start-all.sh
```

**Option B: Start services manually**
```bash
# Unlock Bitwarden and load secrets
export BW_SESSION=$(bw unlock --raw)
source scripts/load-email-env.sh

# Start backend
cd backend
mvn spring-boot:run &

# Start Envoy proxy
cd ..
docker-compose up -d

# Start frontend
cd frontend
npm install
npm run dev
```

### Service Endpoints

Once started, the services will be available at:
- **Frontend**: http://localhost:5173
- **Backend**: localhost:9090 (gRPC)
- **Envoy**: localhost:8080 (gRPC-Web proxy)
- **PostgreSQL**: localhost:5432

### Stop All Services

```bash
./scripts/stop-all.sh
```

Or manually:
```bash
# Stop backend and frontend
lsof -ti :9090 :5173 | xargs kill -9

# Stop Envoy
docker-compose down
```

**Note:** No credentials are hardcoded in the application. All secrets must be in Bitwarden.

## Development

### Quick Start/Stop Commands

```bash
# Start everything
export BW_SESSION=$(bw unlock --raw)
./scripts/start-all.sh

# Stop everything
./scripts/stop-all.sh
```

### Generate gRPC Client Code

After modifying `backend/src/main/proto/email_service.proto`:

```bash
cd frontend
npx --yes protoc --ts_out src/generated --proto_path ../backend/src/main/proto ../backend/src/main/proto/email_service.proto
```

### Test Backend with grpcurl

```bash
# Health check
grpcurl -plaintext localhost:9090 grpc.health.v1.Health/Check

# List inbox (fast - from cache)
grpcurl -plaintext localhost:9090 com.emailclient.backend.grpc.EmailService/ListInbox

# Get email detail (on-demand fetch from Gmail)
grpcurl -plaintext -d '{"id": "176518"}' localhost:9090 com.emailclient.backend.grpc.EmailService/GetEmail

# Send email
grpcurl -plaintext -d '{
  "to": "recipient@example.com",
  "subject": "Test",
  "body": "Hello!",
  "cc": [],
  "bcc": [],
  "attachments": []
}' localhost:9090 com.emailclient.backend.grpc.EmailService/SendEmail
```

## Security Notes

- ✅ **No credentials hardcoded** - All secrets loaded from Bitwarden
- ✅ **Application fails to start** without Bitwarden credentials
- ✅ **PostgreSQL encryption** - Database stores email data securely
- Never commit secrets to git
- Rotate keys if they're ever exposed
- Use Bitwarden or another secure vault for production
- All email data is stored in PostgreSQL with proper access control
- SMTP/IMAP passwords are Google App Passwords (not your main password)
- PostgreSQL volume data is persisted in Docker volumes

## Troubleshooting

**Backend won't start - "Missing required environment variables"**:
```bash
# Verify BW_SESSION is set
echo $BW_SESSION

# If empty, unlock Bitwarden
export BW_SESSION=$(bw unlock --raw)

# Verify Bitwarden item exists
bw get item "Email Client Backend" --session $BW_SESSION

# Check all required fields are present in Bitwarden
source scripts/load-email-env.sh
```

**Backend won't start (port 9090 in use)**:
```bash
./scripts/stop-all.sh
# Or manually:
lsof -ti :9090 | xargs kill -9
```

**Envoy can't reach backend**:
- Ensure backend is running: `lsof -ti :9090`
- Check backend logs: `tail -f /tmp/email-backend.log`
- Verify `host.docker.internal` resolves (macOS/Windows)
- On Linux, use `--network host` or backend container IP

**Frontend can't connect**:
- Verify Envoy is running: `docker ps | grep envoy`
- Test Envoy: `curl http://localhost:8080`
- Check browser console for CORS errors
- Ensure all services are running: `./scripts/start-all.sh`

**Emails not showing full content**:
- First-time views fetch from Gmail (10-20 seconds due to throttling)
- Subsequent views load from cache (< 100ms)
- Check backend logs: `tail -f /tmp/email-backend.log`

## License

Private project - All rights reserved

