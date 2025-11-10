#!/bin/zsh

set -euo pipefail

validate_bw_session() {
  local session="${1:-}"
  if [[ -z "${session}" ]]; then
    return 1
  fi

  local status_json status_val
  status_json=$(bw status --session "${session}" --nointeraction 2>/dev/null || true)
  if [[ -z "${status_json}" ]]; then
    return 1
  fi
  if [[ "${status_json}" == *"You are not logged in."* ]]; then
    return 1
  fi
  status_val=$(echo "${status_json}" | jq -r '.status' 2>/dev/null || echo "")
  [[ "${status_val}" == "unlocked" ]]
}

unlock_with_master_password() {
  if [[ -z "${BW_MASTER_PASSWORD:-}" ]]; then
    return 1
  fi
  echo "🔓 Unlocking Bitwarden vault..."
  export BW_MASTER_PASSWORD
  local unlocked_session
  unlocked_session=$(bw unlock --passwordenv BW_MASTER_PASSWORD --raw --nointeraction 2>/dev/null || true)
  if [[ -n "${unlocked_session}" ]]; then
    if validate_bw_session "${unlocked_session}"; then
      export BW_SESSION="${unlocked_session}"
      return 0
    fi
  fi
  return 1
}

ensure_bw_session() {
  if ! command -v bw >/dev/null 2>&1; then
    echo "❌ Bitwarden CLI is not available inside the container." >&2
    exit 1
  fi

  if [[ -n "${BW_SESSION:-}" ]]; then
    if validate_bw_session "${BW_SESSION}"; then
      return
    fi
    echo "⚠️ Provided BW_SESSION is invalid or locked. Attempting to obtain a new session..."
    unset BW_SESSION
  fi

  local bw_session=""

  # Attempt API-key login (Bitwarden service account / Secrets Manager)
  if [[ -z "${bw_session}" && -n "${BW_CLIENTID:-}" && -n "${BW_CLIENTSECRET:-}" ]]; then
    echo "🔐 Logging into Bitwarden using service-account API key..."
    # bw login --apikey exits 0 even if already logged in, so logout first to avoid stale state
    bw logout >/dev/null 2>&1 || true
    bw_session=$(bw login --apikey --nointeraction --raw 2>/dev/null || true)

    if [[ -z "${bw_session}" ]]; then
      # Older CLI versions do not support --raw with --apikey; fall back to unlock
      bw login --apikey --nointeraction >/dev/null 2>&1 || true
    fi
  fi

  # Email/password login (not recommended; prefer API key)
  if [[ -z "${bw_session}" && -n "${BW_EMAIL:-}" && -n "${BW_MASTER_PASSWORD:-}" ]]; then
    echo "🔐 Logging into Bitwarden using email/password..."
    export BW_MASTER_PASSWORD
    bw_session=$(bw login "${BW_EMAIL}" --passwordenv BW_MASTER_PASSWORD --raw 2>/dev/null || true)
  fi

  # Unlock using provided master password if we are logged in but locked
  if [[ -z "${bw_session}" && -n "${BW_MASTER_PASSWORD:-}" ]]; then
    unlock_with_master_password && return
  fi

  # Expose session to environment if we succeeded
  if [[ -n "${bw_session}" ]]; then
    if validate_bw_session "${bw_session}"; then
      export BW_SESSION="${bw_session}"
      return
    fi
    if unlock_with_master_password; then
      return
    fi
    echo "⚠️ Successfully retrieved a Bitwarden session, but it appears to be locked and no unlock credentials were provided."
  fi

  cat >&2 <<'EOF'
❌ Unable to obtain a Bitwarden session automatically.

Provide one of the following to the backend container (e.g. via docker-compose environment variables or secrets):
  • BW_SESSION – pre-generated Bitwarden session key.
  • BW_CLIENTID and BW_CLIENTSECRET – Bitwarden service-account credentials. Optionally add BW_MASTER_PASSWORD to unlock a personal vault.
  • BW_EMAIL and BW_MASTER_PASSWORD – standard account credentials (less secure; not recommended in production).
EOF
  exit 1
}

if [[ "${USE_BITWARDEN:-true}" == "true" ]]; then
  ensure_bw_session

  if [[ ! -f "/app/scripts/load-email-env.sh" ]]; then
    echo "❌ Unable to locate Bitwarden loader script at /app/scripts/load-email-env.sh" >&2
    exit 1
  fi

  echo "🔐 Loading secrets from Bitwarden inside container..."
  source /app/scripts/load-email-env.sh
fi

DEFAULT_PG_URL="jdbc:postgresql://postgres:5432/emailclient"

if [[ -z "${POSTGRES_URL:-}" ]]; then
  export POSTGRES_URL="${DEFAULT_PG_URL}"
elif [[ "${POSTGRES_URL}" == "jdbc:postgresql://localhost:5432/emailclient" ]]; then
  export POSTGRES_URL="${DEFAULT_PG_URL}"
fi

if [[ -n "${POSTGRES_URL:-}" ]]; then
  export SPRING_DATASOURCE_URL="${POSTGRES_URL}"
fi

if [[ -n "${POSTGRES_USER:-}" ]]; then
  export SPRING_DATASOURCE_USERNAME="${POSTGRES_USER}"
fi

if [[ -n "${POSTGRES_PASSWORD:-}" ]]; then
  export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}"
fi

exec "$@"

