#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [ -f .env ]; then
  echo "→ Carregando .env"
  while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" =~ ^# ]] && continue
    key="$(echo "$key" | xargs)"
    value="$(echo "$value" | xargs)"
    if [ -z "${!key:-}" ]; then
      export "$key=$value"
    fi
  done < .env
else
  echo "⚠  .env não encontrado. Criando a partir de .env.example..."
  cp .env.example .env 2>/dev/null || { echo "❌ .env.example também não existe."; exit 1; }
  while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" =~ ^# ]] && continue
    key="$(echo "$key" | xargs)"
    value="$(echo "$value" | xargs)"
    export "$key=$value"
  done < .env
fi

: "${DB_NAME:=loja}"
: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_USER:=postgres}"
: "${DB_PASS:=postgres}"
: "${REDIS_HOST:=localhost}"
: "${REDIS_PORT:=6379}"
export DB_NAME DB_HOST DB_PORT DB_USER DB_PASS REDIS_HOST REDIS_PORT

wait_for_postgres() {
  local max="${1:-30}"
  local i=0
  echo "→ Aguardando Postgres em ${DB_HOST}:${DB_PORT} (máx ${max}s)..."
  while [ $i -lt "$max" ]; do
    if docker exec loja-postgres pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
      echo "   ✓ Postgres pronto"
      return 0
    fi
    i=$((i+1))
    sleep 1
  done
  echo "⚠  Postgres não respondeu em ${max}s."
  echo "   Verifique: docker logs loja-postgres | tail -20"
  exit 1
}

compose_up() {
  echo "→ Subindo Postgres + Redis..."
  docker compose up -d postgres redis
  wait_for_postgres 30
  echo "→ Estado dos containers:"
  docker compose ps
}

flyway_args() {
  echo "-Dflyway.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
       "-Dflyway.user=${DB_USER}" \
       "-Dflyway.password=${DB_PASS}" \
       "-Dflyway.locations=filesystem:src/main/resources/db/migration"
}

run_flyway() {
  local cmd="$1"; shift || true
  echo "→ Flyway: $cmd"
  # shellcheck disable=SC2046
  ./mvnw "flyway:${cmd}" $(flyway_args) "$@"
}

COMMAND="${1:-}"

case "$COMMAND" in
  ""|run|start|dev)
    compose_up
    echo ""
    echo "→ Migrações disponíveis: $(ls src/main/resources/db/migration/V*.sql 2>/dev/null | wc -l) arquivos"
    run_flyway migrate
    echo ""
    echo "→ Iniciando aplicação (Ctrl+C para parar)..."
    exec ./mvnw spring-boot:run
    ;;
  up)
    compose_up
    run_flyway migrate
    ;;
  down)
    docker compose down
    ;;
  reset)
    if [ "${FORCE:-}" != "1" ]; then
      echo "🚨 reset APAGA o volume do banco. Digite 'APAGAR' para confirmar:"
      read -r conf
      [ "$conf" = "APAGAR" ] || { echo "Cancelado."; exit 1; }
    fi
    docker compose down -v
    compose_up
    run_flyway migrate
    ;;
  logs)
    docker compose logs -f postgres redis
    ;;
  psql)
    docker exec -it loja-postgres psql -U "$DB_USER" -d "$DB_NAME"
    ;;
  info|validate|repair|migrate)
    wait_for_postgres 30
    run_flyway "$COMMAND"
    ;;
  clean)
    if [ "${FORCE:-}" != "1" ]; then
      echo "🚨 flyway:clean APAGA todas as tabelas. Digite 'APAGAR' para confirmar:"
      read -r conf
      [ "$conf" = "APAGAR" ] || { echo "Cancelado."; exit 1; }
    fi
    wait_for_postgres 30
    run_flyway clean
    run_flyway migrate
    ;;
  test|package|verify|install|compile)
    exec ./mvnw "$@"
    ;;
  help|--help|-h)
    sed -n '3,22p' "$0" | sed 's/^# \{0,1\}//'
    exit 0
    ;;
  *)
    exec ./mvnw "$@"
    ;;
esac
