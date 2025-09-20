#!/usr/bin/env bash
# NOTE: Ensure this file has executable permissions (`chmod +x scripts/setup.sh`).
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MARKER="$ROOT_DIR/.setup-complete"

hash_file() { shasum "$1" 2>/dev/null | awk '{print $1}'; }

BACKEND_POM_HASH=$(hash_file "$ROOT_DIR/backend/pom.xml" || echo none)
ANGULAR_LOCK_HASH=$(hash_file "$ROOT_DIR/angular-client/package.json" || echo none)
REACT_LOCK_HASH=$(hash_file "$ROOT_DIR/react-client/package.json" || echo none)

CURRENT_SIGNATURE="backend=$BACKEND_POM_HASH;angular=$ANGULAR_LOCK_HASH;react=$REACT_LOCK_HASH"

if [[ -f "$MARKER" ]] && grep -q "$CURRENT_SIGNATURE" "$MARKER"; then
  echo "[setup] Already complete (signature match). Use --force to rebuild."
  exit 0
fi

echo "[setup] Starting dependency preparation..."

if command -v docker >/dev/null 2>&1; then
  echo "[setup] Docker detected: pre-pulling postgres:latest"
  docker pull postgres:latest >/dev/null 2>&1 || echo "[setup] Warning: could not pull postgres image"
else
  echo "[setup] Docker not found (will rely on local run capabilities)."
fi

echo "[setup] Backend Maven offline warmup"
pushd "$ROOT_DIR/backend" >/dev/null
chmod +x mvnw || true
./mvnw -q -DskipTests dependency:go-offline package
popd >/dev/null

build_frontend () {
  local DIR=$1
  if [[ -f "$DIR/package.json" ]]; then
    echo "[setup] Installing Node deps in $DIR"
    (cd "$DIR" && npm install --no-audit --no-fund && npm run build || echo "[setup] WARNING: build failed for $DIR")
  fi
}

build_frontend "$ROOT_DIR/angular-client"
build_frontend "$ROOT_DIR/react-client"

echo "[setup] Embedding frontend build artifacts into backend static (if present)"
STATIC_DIR="$ROOT_DIR/backend/src/main/resources/static/app"
mkdir -p "$STATIC_DIR"
if [[ -d "$ROOT_DIR/angular-client/dist" ]]; then
  # Find angular dist (could be project name subfolder)
  ANGULAR_DIST=$(find "$ROOT_DIR/angular-client/dist" -maxdepth 1 -type d -not -path '*/dist' | head -n 1)
  [[ -n "$ANGULAR_DIST" ]] && rm -rf "$STATIC_DIR/angular" && cp -R "$ANGULAR_DIST" "$STATIC_DIR/angular"
fi
if [[ -d "$ROOT_DIR/react-client/build" ]]; then
  rm -rf "$STATIC_DIR/react" && cp -R "$ROOT_DIR/react-client/build" "$STATIC_DIR/react"
fi

echo "[setup] Creating signature marker"
echo "$CURRENT_SIGNATURE" > "$MARKER"
echo "[setup] Complete."
