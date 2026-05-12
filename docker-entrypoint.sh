#!/bin/sh
set -e

# Render sets DB_URL=postgresql://user:pass@host[:port]/database — JDBC must NOT embed user:pass in the URL
# (driver treats user:pass@host as invalid authority). User/pass come from DB_USERNAME / DB_PASSWORD.

ssl="${DB_SSL_MODE:-require}"

if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ]; then
  port="${DB_PORT:-5432}"
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${port}/${DB_NAME}?sslmode=${ssl}"
elif [ -n "${DB_URL:-}" ]; then
  raw="$DB_URL"
  case "$raw" in
    jdbc:postgresql:*)
      export SPRING_DATASOURCE_URL="$raw"
      ;;
    postgresql://*|postgres://*)
      rest="${raw#postgresql://}"
      rest="${rest#postgres://}"
      authority="${rest%%/*}"
      tail="${rest#*/}"
      db_and_opts="$tail"
      database="${db_and_opts%%\?*}"
      hostport="${authority##*@}"

      case "$hostport" in
        *:*)
          host_only="${hostport%:*}"
          port_only="${hostport##*:}"
          ;;
        *)
          host_only="$hostport"
          port_only="5432"
          ;;
      esac

      export DB_HOST="${DB_HOST:-$host_only}"
      export DB_PORT="${DB_PORT:-$port_only}"
      export DB_NAME="${DB_NAME:-$database}"
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=${ssl}"
      ;;
    *)
      echo "Unsupported DB_URL scheme (expected postgresql:// or jdbc:postgresql://)"
      exit 1
      ;;
  esac
fi

exec java -jar app.jar
