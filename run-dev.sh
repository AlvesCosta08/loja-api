#!/usr/bin/env bash
# =====================================================================
# run-dev.sh — Loja API (Solução Definitiva)
#
# Uso:
#   ./run-dev.sh               → Sobe tudo (Docker + Flyway) e inicia a App
#   ./run-dev.sh up            → Sobe Docker + Flyway (não inicia a App)
#   ./run-dev.sh down          → Derruba o Docker
#   ./run-dev.sh reset         → APAGA o banco, sobe tudo novamente
#   ./run-dev.sh db <cmd>      → Roda comando Flyway (info, migrate, clean, etc)
#   ./run-dev.sh mvn <args>    → Repassa para o Maven (ex: mvn clean test)
# =====================================================================

set -euo pipefail
cd "$(dirname "$0")"

# ---------- 1. Carrega .env ----------
if [ -f .env ]; then
  while IFS='=' read -r key value; do
    [[ -z "$key" || "$key" =~ ^# ]] && continue
    key="$(echo "$key" | xargs)"
    value="$(echo "$value" | xargs)"
    if [ -z "${!key:-}" ]; then
      export "$key=$value"
    fi
  done < .env
fi

: "${DB_NAME:=loja}"
: "${DB_HOST:=localhost}"
: "${DB_PORT:=5432}"
: "${DB_USER:=postgres}"
: "${DB_PASS:=postgres}"
export DB_NAME DB_HOST DB_PORT DB_USER DB_PASS

# ---------- 2. Utilitários ----------
wait_for_postgres() {
  local max="${1:-30}"
  local i=0
  echo "→ Aguardando Postgres..."
  while [ $i -lt "$max" ]; do
    if docker exec loja-postgres pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
      return 0
    fi
    i=$((i+1))
    sleep 1
  done
  echo "❌ Postgres não respondeu. Verifique: docker logs loja-postgres"
  exit 1
}

flyway_args() {
  echo "-Dflyway.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
       "-Dflyway.user=${DB_USER}" \
       "-Dflyway.password=${DB_PASS}" \
       "-Dflyway.locations=filesystem:src/main/resources/db/migration" \
       "-Dflyway.cleanDisabled=false"
}

# ---------- 3. Roteamento Estrito ----------
COMMAND="${1:-}"

case "$COMMAND" in
  ""|run|start)
    echo "→ Subindo containers..."
    docker compose up -d postgres redis
    wait_for_postgres
    echo "→ Aplicando migrações pendentes..."
    ./mvnw flyway:migrate $(flyway_args) -q
    echo "→ Iniciando Spring Boot..."
    exec ./mvnw spring-boot:run
    ;;

  up)
    docker compose up -d postgres redis
    wait_for_postgres
    ./mvnw flyway:migrate $(flyway_args) -q
    echo "✓ Infraestrutura pronta."
    ;;

  down)
    docker compose down
    echo "✓ Containers derrubados."
    ;;

  reset)
    echo "🚨 Isso APAGARÁ o banco de dados. Digite 'APAGAR' para confirmar:"
    read -r conf
    [ "$conf" = "APAGAR" ] || { echo "Cancelado."; exit 1; }
    docker compose down -v
    docker compose up -d postgres redis
    wait_for_postgres
    ./mvnw flyway:migrate $(flyway_args) -q
    echo "✓ Banco resetado."
    ;;

  db)
    shift
    if [ $# -eq 0 ]; then
      echo "Uso: ./run-dev.sh db <info|migrate|validate|clean>"
      exit 1
    fi
    wait_for_postgres
    # shellcheck disable=SC2046
    ./mvnw "flyway:$1" $(flyway_args)
    ;;

  mvn)
    shift
    exec ./mvnw "$@"
    ;;

  *)
    echo "❌ Comando desconhecido: $COMMAND"
    echo "Use: ./run-dev.sh | up | down | reset | db <cmd> | mvn <args>"
    exit 1
    ;;
esac