#!/usr/bin/env bash
# check_health.sh — waits for all services to be healthy before CI proceeds.
# Usage: ./check_health.sh
# Set env vars to override default URLs:
#   BACKEND_URL, AI_SERVICE_URL, FRONTEND_URL
#
# Exit codes:
#   0 — all services healthy
#   1 — one or more services failed to become healthy within MAX_WAIT seconds

set -euo pipefail

MAX_WAIT=${MAX_WAIT:-120}
INTERVAL=${INTERVAL:-5}
ELAPSED=0

# URLs sourced from environment — never hardcoded
: "${BACKEND_URL:=http://localhost:8080/actuator/health}"
: "${AI_SERVICE_URL:=http://localhost:5000/health}"
: "${FRONTEND_URL:=http://localhost:3000}"

PASS_COUNT=0
FAIL_COUNT=0

print_status() {
  local result=$1 name=$2 url=$3 code=$4
  printf "  [%-4s] %-15s %-45s (HTTP %s)\n" "$result" "$name" "$url" "$code"
}

check_all() {
  PASS_COUNT=0
  FAIL_COUNT=0

  local code

  # Backend actuator health
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$BACKEND_URL" 2>/dev/null || echo "000")
  if [[ "$code" == "200" ]]; then
    print_status "PASS" "Backend" "$BACKEND_URL" "$code"
    ((PASS_COUNT++))
  else
    print_status "FAIL" "Backend" "$BACKEND_URL" "$code"
    ((FAIL_COUNT++))
  fi

  # AI service health
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$AI_SERVICE_URL" 2>/dev/null || echo "000")
  if [[ "$code" == "200" ]]; then
    print_status "PASS" "AI Service" "$AI_SERVICE_URL" "$code"
    ((PASS_COUNT++))
  else
    print_status "FAIL" "AI Service" "$AI_SERVICE_URL" "$code"
    ((FAIL_COUNT++))
  fi

  # Frontend HTTP 200
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$FRONTEND_URL" 2>/dev/null || echo "000")
  if [[ "$code" == "200" ]]; then
    print_status "PASS" "Frontend" "$FRONTEND_URL" "$code"
    ((PASS_COUNT++))
  else
    print_status "FAIL" "Frontend" "$FRONTEND_URL" "$code"
    ((FAIL_COUNT++))
  fi
}

echo "============================================================"
echo " PCI-DSS Compliance Tracker — Service Health Check"
echo " Max wait: ${MAX_WAIT}s | Check interval: ${INTERVAL}s"
echo "============================================================"

while (( ELAPSED < MAX_WAIT )); do
  echo ""
  echo "Checking services at ${ELAPSED}s / ${MAX_WAIT}s ..."
  check_all

  if (( FAIL_COUNT == 0 )); then
    echo ""
    echo "============================================================"
    echo " All ${PASS_COUNT} services are HEALTHY. ✓"
    echo "============================================================"
    exit 0
  fi

  echo ""
  echo "${FAIL_COUNT} service(s) not ready. Retrying in ${INTERVAL}s..."
  sleep "$INTERVAL"
  ((ELAPSED += INTERVAL))
done

echo ""
echo "============================================================"
echo " HEALTH CHECK FAILED after ${MAX_WAIT}s"
echo " ${PASS_COUNT} passed / ${FAIL_COUNT} failed"
echo "============================================================"
exit 1
