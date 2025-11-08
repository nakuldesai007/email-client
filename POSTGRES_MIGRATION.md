# PostgreSQL Migration - From SQLCipher to PostgreSQL

## Summary

Successfully migrated the email client database from **SQLCipher (encrypted SQLite)** to **PostgreSQL** for better scalability, performance, and standard database features.

## What Changed

### 1. Dependencies (pom.xml)
- ❌ REMOVED: `sqlite-jdbc` (SQLCipher)
- ✅ ADDED: `postgresql` driver
- ✅ ADDED: `spring-boot-starter-data-jpa`

### 2. Database Configuration (application.yml)
- ✅ Added Spring datasource configuration for PostgreSQL
- ✅ Added JPA/Hibernate configuration
- ✅ Configured PostgreSQL dialect
- ❌ Removed storage.path and storage.passphrase (SQLCipher specific)

**New Configuration:**
```yaml
spring:
  datasource:
    url: ${POSTGRES_URL}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 3. Email Storage (EmailOfflineStore.java)
- ✅ Replaced custom JDBC connection with Spring DataSource injection
- ✅ Removed SQLCipher-specific PRAGMA statements
- ✅ Updated SQL for PostgreSQL syntax:
  - `TEXT` → `VARCHAR(255)` for id
  - `INTEGER` → `BOOLEAN` for unread flag
  - `BLOB` → `BYTEA` for raw email storage
  - Timestamp handling with `TIMESTAMP WITH TIME ZONE`
- ✅ Added database index for performance
- ✅ Used PostgreSQL's native `ON CONFLICT` syntax

### 4. Properties (EmailClientProperties.java)
- ❌ Removed `path` field from Storage class
- ❌ Removed `passphrase` field from Storage class
- ✅ Kept `previewLimit` field

### 5. Docker Compose (docker-compose.yml)
- ✅ Added PostgreSQL 16 service
- ✅ Configured health checks
- ✅ Added volume for data persistence
- ✅ Made Envoy depend on PostgreSQL health

**New Services:**
```yaml
postgres:
  image: postgres:16-alpine
  ports:
    - "5432:5432"
  volumes:
    - postgres-data:/var/lib/postgresql/data
```

### 6. Bitwarden Configuration (load-email-env.sh)
- ❌ REMOVED: `EMAIL_CLIENT_SQLCIPHER_DB_PATH`
- ❌ REMOVED: `EMAIL_CLIENT_SQLCIPHER_PASSPHRASE`
- ✅ ADDED: `POSTGRES_URL`
- ✅ ADDED: `POSTGRES_USER`
- ✅ ADDED: `POSTGRES_PASSWORD`
- ✅ ADDED: `POSTGRES_DB`

### 7. Scripts Updated
- `setup-bitwarden.sh` - Generates PostgreSQL credentials
- `start-all.sh` - Waits for PostgreSQL to be healthy before starting backend
- `stop-all.sh` - Stops PostgreSQL along with other services

### 8. Documentation Updated
- `README.md` - PostgreSQL in architecture, updated fields
- `BITWARDEN_SETUP.md` - PostgreSQL field documentation
- `POSTGRES_MIGRATION.md` - This file

## Required Bitwarden Fields (Updated)

### REPLACED:
- ❌ `SQLCIPHER_DB_PATH` → ✅ `POSTGRES_URL`
- ❌ `SQLCIPHER_PASSPHRASE` → ✅ `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`

### New PostgreSQL Fields:
```
POSTGRES_URL = jdbc:postgresql://localhost:5432/emailclient
POSTGRES_USER = emailclient
POSTGRES_PASSWORD = (generate with: openssl rand -base64 32 | tr -d '/+=' | head -c 32)
POSTGRES_DB = emailclient
```

## Benefits of PostgreSQL

### Performance
✅ Better query optimizer  
✅ Native indexing support  
✅ Optimized for concurrent connections  
✅ Efficient BYTEA handling for email storage  

### Features
✅ Full ACID compliance  
✅ Advanced transaction support  
✅ Better backup and replication options  
✅ Industry-standard SQL  

### Scalability
✅ Handles larger datasets  
✅ Connection pooling built-in  
✅ Can scale to millions of emails  
✅ Better performance under load  

### Security
✅ Row-level security (future enhancement)  
✅ Role-based access control  
✅ Encrypted connections (SSL/TLS)  
✅ Better audit logging  

## Migration Path

### For Existing Users

If you have existing email data in SQLCipher:

1. **Export from SQLCipher:**
```bash
sqlite3 backend/data/email-client.db "SELECT * FROM email_messages;" > emails.csv
```

2. **Start PostgreSQL:**
```bash
export BW_SESSION=$(bw unlock --raw)
source scripts/load-email-env.sh
docker-compose up -d postgres
```

3. **Import to PostgreSQL:**
```bash
# Connect to PostgreSQL
docker exec -it email-client-postgres-1 psql -U emailclient -d emailclient

# Run import commands (manually adapt your CSV data)
```

4. **Or start fresh** (recommended):
- PostgreSQL will sync latest 50 emails from Gmail automatically
- On-demand fetching will cache emails as you view them

## Testing

### Verify PostgreSQL Connection
```bash
docker exec -it email-client-postgres-1 psql -U emailclient -d emailclient

# Inside psql:
\dt                          # List tables
SELECT COUNT(*) FROM email_messages;  # Count emails
\q                          # Quit
```

### Check Backend Connectivity
```bash
# Start services
export BW_SESSION=$(bw unlock --raw)
./scripts/start-all.sh

# Test gRPC
grpcurl -plaintext localhost:9090 com.emailclient.backend.grpc.EmailService/ListInbox
```

## Rollback (If Needed)

To revert to SQLCipher:
1. Restore `pom.xml` from git
2. Restore `EmailOfflineStore.java` from git
3. Restore `application.yml` from git
4. Restore Bitwarden script from git
5. `mvn clean compile`

## Status

✅ **COMPLETE** - PostgreSQL migration successful  
✅ All services compile without errors  
✅ Database schema auto-created on first run  
✅ All documentation updated  
✅ Bitwarden integration updated  

---

**Migration Date:** November 7, 2025  
**Database:** SQLCipher → PostgreSQL 16  
**Reason:** Better scalability, standard SQL, improved performance

