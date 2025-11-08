#!/bin/zsh

set -euo pipefail

if [[ -z "${BW_SESSION:-}" ]]; then
  echo "❌ Error: BW_SESSION is not set."
  echo "Please unlock Bitwarden first:"
  echo "  export BW_SESSION=\$(bw unlock --raw)"
  return 1
fi

bw_field() {
  bw get item "Email Client Backend" --session "$BW_SESSION" \
    | jq -r --arg name "$1" '.fields[] | select(.name==$name).value'
}

echo "🔐 Loading credentials from Bitwarden..."

# IMAP Configuration
export EMAIL_CLIENT_IMAP_HOST=$(bw_field "IMAP_HOST" || echo "imap.gmail.com")
export EMAIL_CLIENT_IMAP_PORT=$(bw_field "IMAP_PORT" || echo "993")
export EMAIL_CLIENT_IMAP_USER=$(bw_field "IMAP_USER")
export EMAIL_CLIENT_IMAP_PASSWORD=$(bw_field "IMAP_PASSWORD")

# SMTP Configuration
export EMAIL_CLIENT_SMTP_USER=$(bw_field "SMTP_USER")
export EMAIL_CLIENT_SMTP_PASSWORD=$(bw_field "SMTP_PASSWORD")

# PostgreSQL Configuration (no password required for local dev)
export POSTGRES_URL=$(bw_field "POSTGRES_URL" 2>/dev/null || echo "jdbc:postgresql://localhost:5432/emailclient")
export POSTGRES_USER=$(bw_field "POSTGRES_USER" 2>/dev/null || echo "emailclient")
export POSTGRES_PASSWORD=$(bw_field "POSTGRES_PASSWORD" 2>/dev/null || echo "")
export POSTGRES_DB=$(bw_field "POSTGRES_DB" 2>/dev/null || echo "emailclient")

# Crypto Configuration (with built-in defaults)
export EMAIL_CLIENT_MASTER_KEY=$(bw_field "MASTER_KEY" 2>/dev/null || echo "C1pW0G/jHMoCsJ8nGZ4PlgseL2b7Ls0Y4MXq2Qg2cqk=")
export EMAIL_CLIENT_CRYPTO_SALT=$(bw_field "CRYPTO_SALT" 2>/dev/null || echo "bm90LXMtby1zYWx0")

# Legacy fields (optional)
export EMAIL_CLIENT_SMTP_PASSWORD_ENC=$(bw_field "SMTP_PASSWORD_ENC" 2>/dev/null || echo "")
export EMAIL_CLIENT_SMTP_PASSWORD_IV=$(bw_field "SMTP_PASSWORD_IV" 2>/dev/null || echo "")

echo "✅ Environment variables loaded from Bitwarden."
echo ""
echo "Loaded configuration:"
echo "  IMAP: ${EMAIL_CLIENT_IMAP_USER}@${EMAIL_CLIENT_IMAP_HOST}:${EMAIL_CLIENT_IMAP_PORT}"
echo "  SMTP: ${EMAIL_CLIENT_SMTP_USER}@smtp.gmail.com:587"
echo "  Database: PostgreSQL - ${POSTGRES_DB}@localhost:5432"
echo ""

