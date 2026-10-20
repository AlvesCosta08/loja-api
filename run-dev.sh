#!/usr/bin/env bash
# =====================================================================
# run-dev.sh — Loja API (Solução Definitiva)
#
# Uso:
#   ./run-dev.sh               → Sobe tudo (Docker + App)
#   ./run-dev.sh up            → Sobe apenas Docker (sem iniciar a App)
#   ./run-dev.sh down          → Derruba o Docker
#   ./run-dev.sh reset         → APAGA o banco, sobe tudo e recria o admin
#   ./run-dev.sh db <cmd>      → Roda comando Flyway (info, migrate, clean, etc)
#   ./run-dev.sh admin         → Cria/promove o usuário admin padrão
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
  echo "→ Aguardando Postgres ficar pronto..."
  while [ $i -lt "$max" ]; do
    # Usa docker compose exec para ser resiliente ao nome do container
    if docker compose exec -T postgres pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
      echo "✓ Postgres está pronto!"
      return 0
    fi
    i=$((i+1))
    sleep 1
  done
  echo "❌ Postgres não respondeu a tempo. Verifique: docker compose logs postgres"
  exit 1
}

flyway_args() {
  echo "-Dflyway.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
       "-Dflyway.user=${DB_USER}" \
       "-Dflyway.password=${DB_PASS}" \
       "-Dflyway.locations=filesystem:src/main/resources/db/migration" \
       "-Dflyway.cleanDisabled=false"
}

# ---------- 3. Cria ou promove o usuário admin ----------
create_admin() {
  echo "→ Criando/atualizando o usuário admin padrão..."
  # Hash BCrypt REAL para a senha 'admin123'
  docker compose exec -T postgres psql -U "$DB_USER" -d "$DB_NAME" <<EOF
INSERT INTO users (name, email, hashed_password, role)
VALUES (
  'Administrador',
  'admin@loja.com',
  '\$2a\$10\$.GCzcJvStRQ85LketjvdU.5qDW9MbKyCq0AWpX9zKKB44Pvtx6dPy',
  'ADMIN'
)
ON CONFLICT (email) DO UPDATE SET role = 'ADMIN';
EOF
  echo "✓ Admin pronto. Use: admin@loja.com / admin123"
}

# ---------- 4. Roteamento Estrito ----------
COMMAND="${1:-}"

case "$COMMAND" in
  ""|run|start)
    echo "→ Subindo containers (Postgres + Redis)..."
    docker compose up -d postgres redis
    wait_for_postgres
    
    echo "→ Iniciando Spring Boot (o Flyway rodará automaticamente no startup)..."
    # O Spring Boot aplicará as migrations V1-V9 automaticamente ao subir
    exec ./mvnw spring-boot:run
    ;;

  up)
    echo "→ Subindo apenas infraestrutura..."
    docker compose up -d postgres redis
    wait_for_postgres
    echo "→ Aplicando migrações via Flyway..."
    ./mvnw flyway:migrate $(flyway_args) -q
    echo "✓ Infraestrutura pronta e banco atualizado."
    ;;

  down)
    echo "→ Derrubando containers..."
    docker compose down
    echo "✓ Containers derrubados."
    ;;

  reset)
    echo "🚨 Isso APAGARÁ todos os dados do banco local. Digite 'APAGAR' para confirmar:"
    read -r conf
    [ "$conf" = "APAGAR" ] || { echo "Cancelado."; exit 1; }
    
    echo "→ Destruindo volumes e containers..."
    docker compose down -v
    docker compose up -d postgres redis
    wait_for_postgres
    
    echo "→ Reaplicando migrações do zero..."
    ./mvnw flyway:migrate $(flyway_args) -q
    
    create_admin
    echo "✓ Banco resetado e admin recriado com sucesso!"
    ;;

  db)
    shift
    if [ $# -eq 0 ]; then
      echo "Uso: ./run-dev.sh db <info|migrate|validate|clean|repair>"
      exit 1
    fi
    wait_for_postgres
    echo "→ Executando Flyway: $1 ..."
    # shellcheck disable=SC2046
    ./mvnw "flyway:$1" $(flyway_args)
    ;;

  admin)
    wait_for_postgres
    create_admin
    ;;

  mvn)
    shift
    exec ./mvnw "$@"
    ;;

  *)
    echo "❌ Comando desconhecido: $COMMAND"
    echo "Use: ./run-dev.sh | up | down | reset | db <cmd> | admin | mvn <args>"
    exit 1
    ;;
esac