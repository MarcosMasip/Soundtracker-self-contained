#!/usr/bin/env bash
# NOTE: Ensure this file has executable permissions (`chmod +x scripts/start.sh`).
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MARKER="$ROOT_DIR/.setup-complete"

if [[ ! -f "$MARKER" ]]; then
  echo "[start] Setup marker not found. Running setup first..."
  "$ROOT_DIR/scripts/setup.sh"
fi

run_docker() {
  echo "[start] Launching via docker-compose (mock profile)"
  SPRING_PROFILES_ACTIVE=mock docker compose up -d || return 1
  echo "[start] Waiting for backend health"
  for i in {1..30}; do
    if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
      echo "[start] Backend is up."; return 0; fi
    sleep 2
  done
  echo "[start] Backend did not become healthy in time." >&2
  return 1
}

if command -v docker >/dev/null 2>&1; then
  if run_docker; then
    echo "[start] URLs:";
    echo "  API:    http://localhost:8080/swagger-ui.html";
    echo "  Angular static (if built): http://localhost:8080/app/angular/";
    echo "  React static (if built):   http://localhost:8080/app/react/";
    exit 0
  else
    echo "[start] Docker path failed, falling back to local run." >&2
  fi
fi

echo "[start] Running backend locally (H2 / local profile)"
pushd "$ROOT_DIR/backend" >/dev/null
./mvnw -q -Dspring-boot.run.profiles=local spring-boot:run
popd >/dev/null
