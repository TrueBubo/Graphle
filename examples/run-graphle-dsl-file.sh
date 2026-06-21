#!/usr/bin/env bash
set -euo pipefail

DEFAULT_SAMPLE_ROOT=~/GraphleDslSample

usage() {
  printf 'Usage: %s <dsl-file> [graphle-base-url] [sample-root]\n' "$0"
  printf 'Defaults: http://localhost:5824 and ~/GraphleDslSample\n'
  printf '{{SAMPLE_ROOT}} placeholders are replaced before execution.\n'
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -lt 1 || $# -gt 3 ]]; then
  usage >&2
  exit 1
fi

DSL_FILE="$1"
BASE_URL="${2:-http://localhost:5824}"
SAMPLE_ROOT="${3:-$DEFAULT_SAMPLE_ROOT}"

if [[ "$BASE_URL" == */dsl || "$BASE_URL" == */dsl/ ]]; then
  printf 'Expected Graphle base URL, not DSL endpoint: %s\n' "$BASE_URL" >&2
  printf 'Use a value such as http://localhost:5824\n' >&2
  exit 1
fi

DSL_URL="${BASE_URL%/}/dsl"

if [[ ! -f "$DSL_FILE" ]]; then
  printf 'DSL file not found: %s\n' "$DSL_FILE" >&2
  exit 1
fi

json_escape() {
  local value="$1"
  value=${value//\\/\\\\}
  value=${value//\"/\\\"}
  value=${value//$'\n'/\\n}
  value=${value//$'\r'/\\r}
  value=${value//$'\t'/\\t}
  printf '%s' "$value"
}

escape_dsl_value() {
  local value="$1"
  value=${value//\\/\\\\}
  value=${value//\"/\\\"}
  printf '%s' "$value"
}

run_dsl() {
  local command="$1"
  local payload response
  payload="{\"command\":\"$(json_escape "$command")\"}"

  printf 'DSL> %s\n' "$command"
  response="$(curl -fsS \
    -H 'Content-Type: application/json' \
    --data "$payload" \
    "$DSL_URL")"
  printf '%s\n\n' "$response"

  if [[ "$response" == *'"type":"ERROR"'* ]]; then
    printf 'Graphle returned an error for command: %s\n' "$command" >&2
    exit 1
  fi
}

escaped_sample_root="$(escape_dsl_value "$SAMPLE_ROOT")"

while IFS= read -r line || [[ -n "$line" ]]; do
  [[ "$line" =~ ^[[:space:]]*($|#) ]] && continue

  command="${line//\{\{SAMPLE_ROOT\}\}/$escaped_sample_root}"
  run_dsl "$command"
done < "$DSL_FILE"

printf 'Graphle DSL file executed: %s\n' "$DSL_FILE"
