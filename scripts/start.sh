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
    cat <<'EOF'
[start] =============================================================
[start] Soundtracker (Docker / mock profile)
[start] -------------------------------------------------------------
[start] Web UI / Docs:
[start]   Swagger UI:      http://localhost:8080/swagger-ui.html
[start]   Angular (static): http://localhost:8080/app/angular/    (if built)
[start]   React   (static): http://localhost:8080/app/react/      (if built)
[start]
[start] Quick API Smoke Tests (copy/paste):
[start]   curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
[start]   curl -s http://localhost:8080/api-soundtracker/db-movie/all-movies-dto | jq '.[0]' 2>/dev/null || curl -s http://localhost:8080/api-soundtracker/db-movie/all-movies-dto
[start]
[start] Auth (example sign-up & sign-in):
[start]   curl -s -X POST -H 'Content-Type: application/json' \\
[start]     -d '{"username":"demo","password":"demo"}' \\
[start]     http://localhost:8080/api/auth/sign-up
[start]   curl -s -X POST -H 'Content-Type: application/json' \\
[start]     -d '{"username":"demo","password":"demo"}' \\
[start]     http://localhost:8080/api/auth/sign-in
[start]
[start] Stopping (Docker path):
[start]   docker compose down    # keep data volume
[start]   docker compose down -v # remove Postgres data
[start]
[start] Profile Summary: running with 'mock' profile (Postgres + fixtures, stub external APIs)
[start] =============================================================
EOF
    exit 0
  else
    echo "[start] Docker path failed, falling back to local run." >&2
  fi
fi

cat <<'EOF'
[start] =============================================================
[start] Soundtracker (Local / H2 / local profile)
[start] -------------------------------------------------------------
[start] The application will now start in the foreground.
[start] Once you see 'Started BackendApplication', open:
[start]   Swagger UI:      http://localhost:8080/swagger-ui.html
[start]   Angular (static): http://localhost:8080/app/angular/    (if built)
[start]   React   (static): http://localhost:8080/app/react/      (if built)
[start]
[start] Quick checks (run in new terminal):
[start]   curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
[start]   curl -s http://localhost:8080/api-soundtracker/db-movie/all-movies-dto | jq '.[0]' 2>/dev/null || curl -s http://localhost:8080/api-soundtracker/db-movie/all-movies-dto
[start]
[start] Auth flows (example):
[start]   curl -s -X POST -H 'Content-Type: application/json' \\
[start]     -d '{"username":"demo","password":"demo"}' \\
[start]     http://localhost:8080/api/auth/sign-up
[start]   curl -s -X POST -H 'Content-Type: application/json' \\
[start]     -d '{"username":"demo","password":"demo"}' \\
[start]     http://localhost:8080/api/auth/sign-in
[start]
[start] Stopping local run: Ctrl + C in this terminal.
[start] Profile Summary: running with 'local' profile (H2 + fixtures, stub external APIs)
[start] =============================================================
EOF

echo "[start] Running backend locally (H2 / local profile)"
pushd "$ROOT_DIR/backend" >/dev/null
./mvnw -q -Dspring-boot.run.profiles=local spring-boot:run
popd >/dev/null
