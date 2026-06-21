#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MANAGER_DIR="$ROOT_DIR/GraphleManager"
UI_DIR="$ROOT_DIR/GraphleUI"
COMPOSE_FILE="$MANAGER_DIR/compose.yaml"

NEO4J_PORT=7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=notverysecret
VALKEY_PORT=6379
BACKEND_PORT=5824

detect_compose() {
  if docker compose version >/dev/null 2>&1; then
    COMPOSE=(docker compose)
  elif command -v docker-compose >/dev/null 2>&1; then
    COMPOSE=(docker-compose)
  else
    echo "Error: neither 'docker compose' nor 'docker-compose' is available." >&2
    echo "Install Docker (with the Compose plugin) and try again." >&2
    exit 1
  fi
  echo "Using Compose command: ${COMPOSE[*]}"
}

compose() {
  "${COMPOSE[@]}" -f "$COMPOSE_FILE" "$@"
}

wait_for_port() {
  local name="$1" host="$2" port="$3" timeout="${4:-60}"
  printf 'Waiting for %s (%s:%s) ' "$name" "$host" "$port"
  for ((i = 0; i < timeout; i++)); do
    if (exec 3<>"/dev/tcp/$host/$port") 2>/dev/null; then
      exec 3>&- 3<&- 2>/dev/null || true
      echo " ready."
      return 0
    fi
    printf '.'
    sleep 1
  done
  echo
  echo "Error: $name did not become ready within ${timeout}s." >&2
  return 1
}

wait_for_neo4j() {
  local timeout="${1:-120}"
  wait_for_port "Neo4j Bolt socket" 127.0.0.1 "$NEO4J_PORT" "$timeout"

  printf 'Waiting for Neo4j Cypher readiness '
  for ((i = 0; i < timeout; i++)); do
    if compose exec -T neo4j cypher-shell -u "$NEO4J_USER" -p "$NEO4J_PASSWORD" "RETURN 1;" >/dev/null 2>&1; then
      echo " ready."
      return 0
    fi
    printf '.'
    sleep 1
  done
  echo
  echo "Error: Neo4j did not accept Cypher queries within ${timeout}s." >&2
  echo "Run '${COMPOSE[*]} -f \"$COMPOSE_FILE\" logs neo4j' for details." >&2
  return 1
}

kill_tree() {
  local pid="$1" child
  for child in $(pgrep -P "$pid" 2>/dev/null); do
    kill_tree "$child"
  done
  kill "$pid" 2>/dev/null || true
}

BACKEND_PID=""
UI_PID=""

cleanup() {
  trap - INT TERM EXIT
  echo
  echo "Shutting down..."
  [[ -n "$UI_PID" ]] && kill_tree "$UI_PID"
  [[ -n "$BACKEND_PID" ]] && kill_tree "$BACKEND_PID"
  compose down
  echo "Done."
}

detect_compose
trap cleanup INT TERM EXIT

echo "Starting Neo4j and Valkey..."
compose up -d

wait_for_neo4j 120
wait_for_port "Valkey" 127.0.0.1 "$VALKEY_PORT" 30

echo "Starting GraphleManager backend..."
(cd "$MANAGER_DIR" && ./gradlew bootRun) &
BACKEND_PID=$!

wait_for_port "GraphleManager" 127.0.0.1 "$BACKEND_PORT" 180

echo "Starting GraphleUI..."
(cd "$UI_DIR" && ./gradlew run) &
UI_PID=$!

wait "$UI_PID"
