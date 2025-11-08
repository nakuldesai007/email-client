# Bitwarden Configuration Guide

## Overview

This email client requires **ALL** credentials to be stored in Bitwarden. No credentials are hardcoded in the application for security.

## Required Bitwarden Item

**Item Name:** `Email Client Backend`

Add this item to your Bitwarden vault with the following **custom fields**:

### Core Email Configuration

| Field Name          | Example Value              | How to Get It                                                    |
|---------------------|----------------------------|------------------------------------------------------------------|
| `IMAP_HOST`         | imap.gmail.com             | Gmail IMAP server (standard)                                     |
| `IMAP_PORT`         | 993                        | Gmail IMAP port (standard)                                       |
| `IMAP_USER`         | your_email@gmail.com       | Your Gmail address                                               |
| `IMAP_PASSWORD`     | abcd efgh ijkl mnop        | Google App Password (16 chars, Settings → Security → App Passwords) |
| `SMTP_USER`         | your_email@gmail.com       | Your Gmail address (same as IMAP_USER)                           |
| `SMTP_PASSWORD`     | abcd efgh ijkl mnop        | Google App Password (same as IMAP_PASSWORD)                      |

### PostgreSQL Configuration

| Field Name          | Example Value                                  | How to Generate/Configure            |
|---------------------|-----------------------------------------------|--------------------------------------|
| `POSTGRES_URL`      | jdbc:postgresql://localhost:5432/emailclient  | JDBC URL for PostgreSQL              |
| `POSTGRES_USER`     | emailclient                                   | Database username                    |
| `POSTGRES_PASSWORD` | (32 char random string)                       | `openssl rand -base64 32 \| tr -d '/+=' \| head -c 32` |
| `POSTGRES_DB`       | emailclient                                   | Database name                        |

### Security Configuration

| Field Name               | Example Value                           | How to Generate                      |
|--------------------------|----------------------------------------|--------------------------------------|
| `MASTER_KEY`             | (44 char random string)                | `openssl rand -base64 32`            |
| `CRYPTO_SALT`            | (24 char random string)                | `openssl rand -base64 16`            |

### Legacy Fields (Optional)

| Field Name               | Value                 | Note                                    |
|--------------------------|-----------------------|-----------------------------------------|
| `SMTP_PASSWORD_ENC`      | (can be empty)        | Legacy encrypted password - not used    |
| `SMTP_PASSWORD_IV`       | (can be empty)        | Legacy IV - not used                    |

## Setup Steps

### 1. Generate Secure Keys

Run this command to generate all required cryptographic keys:

```bash
./scripts/setup-bitwarden.sh
```

This will:
- Prompt for your Gmail credentials
- Generate secure encryption keys
- Display all values to add to Bitwarden

### 2. Add Fields to Bitwarden

1. Open Bitwarden (web, desktop, or CLI)
2. Create or edit the item named **"Email Client Backend"**
3. Add each field as a **Custom Field** with type **Text** (or **Hidden** for passwords)
4. Copy the exact values from the setup script output

### 3. Verify Setup

```bash
# Unlock Bitwarden
export BW_SESSION=$(bw unlock --raw)

# Test loading credentials
source scripts/load-email-env.sh
```

You should see:
```
✅ Environment variables loaded from Bitwarden.

Loaded configuration:
  IMAP: your_email@imap.gmail.com:993
  SMTP: your_email@smtp.gmail.com:587
  Database: ./data/email-client.db
```

### 4. Start the Application

```bash
export BW_SESSION=$(bw unlock --raw)
./scripts/start-all.sh
```

## Security Benefits

✅ **Zero hardcoded credentials** - All secrets in secure vault  
✅ **Application fails safely** - Won't start without proper credentials  
✅ **Easy credential rotation** - Update in Bitwarden, restart app  
✅ **Team sharing** - Share Bitwarden item securely  
✅ **Audit trail** - Bitwarden tracks access/changes  

## Troubleshooting

### "Missing required environment variables"

**Cause:** One or more fields are missing in Bitwarden

**Solution:**
```bash
# Check what's in Bitwarden
export BW_SESSION=$(bw unlock --raw)
bw get item "Email Client Backend" --session $BW_SESSION | jq '.fields'

# Compare with required fields above
```

### "BW_SESSION is not set"

**Solution:**
```bash
export BW_SESSION=$(bw unlock --raw)
# Enter your Bitwarden master password when prompted
```

### Values not loading from Bitwarden

**Cause:** Field names don't match exactly (case-sensitive!)

**Solution:**
- Field names must be **exact matches** (e.g., `IMAP_USER` not `imap_user`)
- Field type should be **Text** or **Hidden**
- Item name must be exactly **"Email Client Backend"**

## Field Reference

Here's the complete list you can copy-paste when creating fields in Bitwarden:

```
IMAP_HOST
IMAP_PORT
IMAP_USER
IMAP_PASSWORD
SMTP_USER
SMTP_PASSWORD
POSTGRES_URL
POSTGRES_USER
POSTGRES_PASSWORD
POSTGRES_DB
MASTER_KEY
CRYPTO_SALT
SMTP_PASSWORD_ENC
SMTP_PASSWORD_IV
```

---

**Remember:** Keep your Bitwarden master password secure! It protects all your email client credentials.

