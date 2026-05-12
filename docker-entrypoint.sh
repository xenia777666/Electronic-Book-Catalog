#!/bin/sh
set -e

# Render managed Postgres sets DB_URL as postgresql://user:pass@host/db — JDBC needs jdbc:postgresql://...
db_url="${DB_URL:-}"
case "$db_url" in
  postgresql://*)
    export SPRING_DATASOURCE_URL="jdbc:${db_url}"
    ;;
  postgres://*)
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${db_url#postgres://}"
    ;;
esac

exec java -jar app.jar
