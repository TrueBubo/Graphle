#!/usr/bin/env bash
set -euo pipefail

DEFAULT_SAMPLE_ROOT=~/GraphleDslSample

usage() {
  printf 'Usage: %s [graphle-base-url] [sample-root]\n' "$0"
  printf 'Defaults: http://localhost:5824 and ~/GraphleDslSample\n'
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

BASE_URL="${1:-http://localhost:5824}"
SAMPLE_ROOT="${2:-$DEFAULT_SAMPLE_ROOT}"

DSL_URL="${BASE_URL%/}/dsl"

json_escape() {
  local value="$1"
  value=${value//\\/\\\\}
  value=${value//\"/\\\"}
  value=${value//$'\n'/\\n}
  value=${value//$'\r'/\\r}
  value=${value//$'\t'/\\t}
  printf '%s' "$value"
}

dsl_quote() {
  local value="$1"
  value=${value//\\/\\\\}
  value=${value//\"/\\\"}
  printf '"%s"' "$value"
}

run_dsl() {
  local command="$1"
  local payload response
  payload="{\"command\":\"$(json_escape "$command")\"}"

  printf 'DSL> %s\n' "$command"
  response="$(curl \
    -H 'Content-Type: application/json' \
    -X POST \
    -d "$payload" \
    "$DSL_URL")"
  printf '%s\n\n' "$response"

  if [[ "$response" == *'"type":"ERROR"'* ]]; then
    printf 'Graphle returned an error for command: %s\n' "$command" >&2
    exit 1
  fi
}

add_tag() {
  local path="$1"
  local name="$2"
  local value="${3:-}"

  if [[ $# -eq 3 ]]; then
    run_dsl "addTag $(dsl_quote "$path") $(dsl_quote "$name") $(dsl_quote "$value")"
  else
    run_dsl "addTag $(dsl_quote "$path") $(dsl_quote "$name")"
  fi
}

add_relationship() {
  local from="$1"
  local to="$2"
  local name="$3"
  local value="${4:-}"

  if [[ $# -eq 4 ]]; then
    run_dsl "addRel $(dsl_quote "$from") $(dsl_quote "$to") $(dsl_quote "$name") $(dsl_quote "$value")"
  else
    run_dsl "addRel $(dsl_quote "$from") $(dsl_quote "$to") $(dsl_quote "$name")"
  fi
}

mkdir -p \
  "$SAMPLE_ROOT/research/papers" \
  "$SAMPLE_ROOT/research/notes" \
  "$SAMPLE_ROOT/research/datasets" \
  "$SAMPLE_ROOT/projects/graphle-demo" \
  "$SAMPLE_ROOT/movies" \
  "$SAMPLE_ROOT/archive"

touch \
  "$SAMPLE_ROOT/research/papers/graph-databases-survey.pdf" \
  "$SAMPLE_ROOT/research/notes/literature-review.md" \
  "$SAMPLE_ROOT/research/notes/research-questions.md" \
  "$SAMPLE_ROOT/research/datasets/interviews.csv" \
  "$SAMPLE_ROOT/projects/graphle-demo/experiment-plan.md" \
  "$SAMPLE_ROOT/projects/graphle-demo/results-summary.md" \
  "$SAMPLE_ROOT/projects/graphle-demo/presentation-outline.md" \
  "$SAMPLE_ROOT/movies/the-matrix.mkv" \
  "$SAMPLE_ROOT/movies/spirited-away.mkv" \
  "$SAMPLE_ROOT/movies/arrival.mkv" \
  "$SAMPLE_ROOT/movies/everything-everywhere-all-at-once.mkv" \
  "$SAMPLE_ROOT/movies/the-godfather.mkv" \
  "$SAMPLE_ROOT/movies/star-wars-a-new-hope.mkv" \
  "$SAMPLE_ROOT/movies/jurassic-park.mkv" \
  "$SAMPLE_ROOT/movies/pulp-fiction.mkv" \
  "$SAMPLE_ROOT/movies/forrest-gump.mkv" \
  "$SAMPLE_ROOT/movies/titanic.mkv" \
  "$SAMPLE_ROOT/movies/avatar.mkv" \
  "$SAMPLE_ROOT/movies/the-dark-knight.mkv" \
  "$SAMPLE_ROOT/movies/inception.mkv" \
  "$SAMPLE_ROOT/movies/interstellar.mkv" \
  "$SAMPLE_ROOT/movies/parasite.mkv" \
  "$SAMPLE_ROOT/movies/the-lord-of-the-rings-the-fellowship-of-the-ring.mkv" \
  "$SAMPLE_ROOT/archive/old-notes.txt"

SURVEY="$SAMPLE_ROOT/research/papers/graph-databases-survey.pdf"
LITERATURE_REVIEW="$SAMPLE_ROOT/research/notes/literature-review.md"
QUESTIONS="$SAMPLE_ROOT/research/notes/research-questions.md"
DATASET="$SAMPLE_ROOT/research/datasets/interviews.csv"
EXPERIMENT_PLAN="$SAMPLE_ROOT/projects/graphle-demo/experiment-plan.md"
RESULTS="$SAMPLE_ROOT/projects/graphle-demo/results-summary.md"
PRESENTATION="$SAMPLE_ROOT/projects/graphle-demo/presentation-outline.md"
THE_MATRIX="$SAMPLE_ROOT/movies/the-matrix.mkv"
SPIRITED_AWAY="$SAMPLE_ROOT/movies/spirited-away.mkv"
ARRIVAL="$SAMPLE_ROOT/movies/arrival.mkv"
EVERYTHING_EVERYWHERE="$SAMPLE_ROOT/movies/everything-everywhere-all-at-once.mkv"
THE_GODFATHER="$SAMPLE_ROOT/movies/the-godfather.mkv"
STAR_WARS_A_NEW_HOPE="$SAMPLE_ROOT/movies/star-wars-a-new-hope.mkv"
JURASSIC_PARK="$SAMPLE_ROOT/movies/jurassic-park.mkv"
PULP_FICTION="$SAMPLE_ROOT/movies/pulp-fiction.mkv"
FORREST_GUMP="$SAMPLE_ROOT/movies/forrest-gump.mkv"
TITANIC="$SAMPLE_ROOT/movies/titanic.mkv"
AVATAR="$SAMPLE_ROOT/movies/avatar.mkv"
THE_DARK_KNIGHT="$SAMPLE_ROOT/movies/the-dark-knight.mkv"
INCEPTION="$SAMPLE_ROOT/movies/inception.mkv"
INTERSTELLAR="$SAMPLE_ROOT/movies/interstellar.mkv"
PARASITE="$SAMPLE_ROOT/movies/parasite.mkv"
LOTR_FELLOWSHIP="$SAMPLE_ROOT/movies/the-lord-of-the-rings-the-fellowship-of-the-ring.mkv"
OLD_NOTES="$SAMPLE_ROOT/archive/old-notes.txt"

add_tag "$SAMPLE_ROOT" "sample" "graphle-dsl"
add_tag "$SAMPLE_ROOT/research" "area" "research"
add_tag "$SAMPLE_ROOT/projects/graphle-demo" "project" "graphle-demo"
add_tag "$SAMPLE_ROOT/movies" "collection" "movies"

add_tag "$SURVEY" "type" "paper"
add_tag "$SURVEY" "topic" "graph-databases"
add_tag "$SURVEY" "url" "https://neo4j.com/docs/getting-started/graph-database/"
add_tag "$LITERATURE_REVIEW" "type" "note"
add_tag "$LITERATURE_REVIEW" "status" "draft"
add_tag "$QUESTIONS" "type" "note"
add_tag "$QUESTIONS" "status" "open"
add_tag "$DATASET" "type" "dataset"
add_tag "$DATASET" "source" "interviews"
add_tag "$EXPERIMENT_PLAN" "type" "plan"
add_tag "$EXPERIMENT_PLAN" "project" "graphle-demo"
add_tag "$RESULTS" "type" "summary"
add_tag "$RESULTS" "project" "graphle-demo"
add_tag "$PRESENTATION" "type" "outline"
add_tag "$PRESENTATION" "project" "graphle-demo"
add_tag "$OLD_NOTES" "status" "archived"

add_tag "$THE_MATRIX" "type" "movie"
add_tag "$THE_MATRIX" "year" "1999"
add_tag "$SPIRITED_AWAY" "type" "movie"
add_tag "$SPIRITED_AWAY" "year" "2001"
add_tag "$ARRIVAL" "type" "movie"
add_tag "$ARRIVAL" "year" "2016"
add_tag "$EVERYTHING_EVERYWHERE" "type" "movie"
add_tag "$EVERYTHING_EVERYWHERE" "year" "2022"
add_tag "$THE_GODFATHER" "type" "movie"
add_tag "$THE_GODFATHER" "year" "1972"
add_tag "$STAR_WARS_A_NEW_HOPE" "type" "movie"
add_tag "$STAR_WARS_A_NEW_HOPE" "year" "1977"
add_tag "$JURASSIC_PARK" "type" "movie"
add_tag "$JURASSIC_PARK" "year" "1993"
add_tag "$PULP_FICTION" "type" "movie"
add_tag "$PULP_FICTION" "year" "1994"
add_tag "$FORREST_GUMP" "type" "movie"
add_tag "$FORREST_GUMP" "year" "1994"
add_tag "$TITANIC" "type" "movie"
add_tag "$TITANIC" "year" "1997"
add_tag "$AVATAR" "type" "movie"
add_tag "$AVATAR" "year" "2009"
add_tag "$THE_DARK_KNIGHT" "type" "movie"
add_tag "$THE_DARK_KNIGHT" "year" "2008"
add_tag "$INCEPTION" "type" "movie"
add_tag "$INCEPTION" "year" "2010"
add_tag "$INTERSTELLAR" "type" "movie"
add_tag "$INTERSTELLAR" "year" "2014"
add_tag "$PARASITE" "type" "movie"
add_tag "$PARASITE" "year" "2019"
add_tag "$LOTR_FELLOWSHIP" "type" "movie"
add_tag "$LOTR_FELLOWSHIP" "year" "2001"

add_relationship "$SURVEY" "$LITERATURE_REVIEW" "supports" "background"
add_relationship "$LITERATURE_REVIEW" "$QUESTIONS" "raises"
add_relationship "$QUESTIONS" "$EXPERIMENT_PLAN" "motivates"
add_relationship "$DATASET" "$EXPERIMENT_PLAN" "input-for"
add_relationship "$EXPERIMENT_PLAN" "$RESULTS" "produces"
add_relationship "$RESULTS" "$PRESENTATION" "summarized-by"
add_relationship "$OLD_NOTES" "$LITERATURE_REVIEW" "historical-context"

printf 'Created sample filesystem tree at %s\n' "$SAMPLE_ROOT"
