#!/bin/zsh

set -euo pipefail

echo "🔐 Bitwarden Email Client Setup"
echo "================================"
echo ""

# Check if BW_SESSION is set
if [[ -z "${BW_SESSION:-}" ]]; then
  echo "❌ Error: BW_SESSION is not set."
  echo "Please unlock Bitwarden first:"
  echo "  export BW_SESSION=\$(bw unlock --raw)"
  exit 1
fi

# Function to add or update a field in Bitwarden
update_bw_field() {
  local field_name="$1"
  local field_value="$2"
  
  echo "  Setting ${field_name}..."
  
  # Get the item
  local item=$(bw get item "Email Client Backend" --session "$BW_SESSION" 2>/dev/null || echo "")
  
  if [[ -z "$item" ]]; then
    echo "❌ Error: Bitwarden item 'Email Client Backend' not found."
    echo "Please create this item in Bitwarden first."
    exit 1
  fi
  
  # Update the field using bw CLI
  # Note: This is a simplified version - you may need to use the web UI or API for complex updates
  echo "    $field_name = $field_value"
}

echo "Enter your Gmail credentials:"
echo ""

# Prompt for Gmail credentials
read -r "gmail_address?Gmail address (e.g., user@gmail.com): "
read -r -s "gmail_app_password?Gmail App Password: "
echo ""

# Generate secure keys
echo ""
echo "🔑 Generating secure cryptographic keys..."
MASTER_KEY=$(openssl rand -base64 32)
CRYPTO_SALT=$(openssl rand -base64 16)
POSTGRES_PASSWORD=$(openssl rand -base64 32 | tr -d '/+=' | head -c 32)

echo ""
echo "📋 Required Bitwarden Fields:"
echo "================================"
echo ""
echo "Please add these custom fields to your Bitwarden item 'Email Client Backend':"
echo ""
echo "IMAP Configuration:"
echo "  IMAP_HOST = imap.gmail.com"
echo "  IMAP_PORT = 993"
echo "  IMAP_USER = $gmail_address"
echo "  IMAP_PASSWORD = $gmail_app_password"
echo ""
echo "SMTP Configuration:"
echo "  SMTP_USER = $gmail_address"
echo "  SMTP_PASSWORD = $gmail_app_password"
echo ""
echo "PostgreSQL Configuration:"
echo "  POSTGRES_URL = jdbc:postgresql://localhost:5432/emailclient"
echo "  POSTGRES_USER = emailclient"
echo "  POSTGRES_PASSWORD = $POSTGRES_PASSWORD"
echo "  POSTGRES_DB = emailclient"
echo ""
echo "Crypto Configuration:"
echo "  MASTER_KEY = $MASTER_KEY"
echo "  CRYPTO_SALT = $CRYPTO_SALT"
echo ""
echo "Legacy Fields (can be empty):"
echo "  SMTP_PASSWORD_ENC = (leave empty)"
echo "  SMTP_PASSWORD_IV = (leave empty)"
echo ""
echo "================================"
echo ""
echo "⚠️  IMPORTANT: Copy the values above and add them as custom fields"
echo "    in your Bitwarden item 'Email Client Backend'"
echo ""
echo "Once added, run:"
echo "  export BW_SESSION=\$(bw unlock --raw)"
echo "  source scripts/load-email-env.sh"
echo "  cd backend && mvn spring-boot:run"
echo ""

