# Soundtracker (fullstack application)

The purpose of the project is to create a fullstack web application. Subject area -  music and films.<br>
## Offline / Mock Mode

This fork has been enhanced to be fully self-contained:

Key properties:
- No external API calls required in default `mock` profile.
- Deterministic dataset seeded from classpath fixtures on first run.
- Frontend bundles (Angular, React) can be embedded and served from the backend.
- Two universal commands:
	1. `./scripts/setup.sh` (or `scripts\setup.ps1` on Windows) – downloads/builds everything and embeds frontends.
	2. `./scripts/start.sh` (or `scripts\start.ps1`) – starts via Docker if available, otherwise local H2 fallback.

### Quick Start
macOS / Linux:
```
# If you see 'permission denied', either add execute bit or invoke via bash.
bash scripts/setup.sh
bash scripts/start.sh
```

Windows (PowerShell):
```
scripts\setup.ps1
scripts\start.ps1
```

## Step-by-Step Onboarding (Cross-Platform)

This section gives a literal, copy/paste friendly path from zero to a running, fully offline instance.

### 1. Prerequisites
Mandatory (first run must be online once to cache artifacts):
- Java 17+ (verify: `java -version`)
- Git
- Internet connectivity for the very first `setup` run (after that: optional)

Optional but recommended:
- Docker Desktop (macOS/Windows) OR Docker Engine (Linux) for Postgres container; otherwise H2 fallback is used.
- Node.js 18+ & npm (only needed if you plan to rebuild the Angular/React frontends; pre-built bundles can be reused once produced).
- `jq` command-line JSON processor (for pretty printing API responses in examples below).

### 2. Clone the repository
macOS / Linux:
```
git clone https://github.com/<your-org-or-user>/Soundtracker-self-contained.git
cd Soundtracker-self-contained
```
Windows (PowerShell):
```
git clone https://github.com/<your-org-or-user>/Soundtracker-self-contained.git
cd Soundtracker-self-contained
```

### 3. (macOS/Linux) Ensure script permissions (skip if already executable)
```
chmod +x scripts/*.sh backend/mvnw
```
If you forget this and see `permission denied`, either run the chmod above or invoke via `bash scripts/setup.sh`.

### 4. Run the one-time setup (caches everything)
macOS / Linux:
```
bash scripts/setup.sh
```
Windows (PowerShell):
```
scripts\setup.ps1
```
What happens:
1. Computes a signature of backend & frontend dependency manifests.
2. Pre-pulls the Postgres Docker image (if Docker is present).
3. Downloads all Maven dependencies (offline warmup) and builds backend JAR.
4. Installs Node dependencies & builds Angular / React (if their `package.json` exists).
5. Copies built frontend assets into `backend/src/main/resources/static/app/<framework>` for offline serving.
6. Writes `.setup-complete` with the dependency signature so subsequent runs are instant unless something changed.

Expected (abridged) output sample:
```
[setup] Starting dependency preparation...
[setup] Docker detected: pre-pulling postgres:latest  # (or 'Docker not found ...')
[setup] Backend Maven offline warmup
[setup] Installing Node deps in .../angular-client
[setup] Installing Node deps in .../react-client
[setup] Embedding frontend build artifacts into backend static (if present)
[setup] Creating signature marker
[setup] Complete.
```
Re-running without changes:
```
bash scripts/setup.sh
[setup] Already complete (signature match). Use --force to rebuild.
```
Force a rebuild after changing dependencies:
```
bash scripts/setup.sh --force     # macOS/Linux
scripts\setup.ps1 -Force          # Windows
```

### 5. Start the stack
macOS / Linux:
```
bash scripts/start.sh
```
Windows (PowerShell):
```
scripts\start.ps1
```
Behavior:
1. If Docker is available: launches Postgres + backend with profile `mock`.
2. Waits for health endpoint to become UP.
3. Prints useful URLs.
4. If Docker is not available or fails, falls back to in-process Spring Boot with H2 using profile `local`.
 5. A landing page is now served at `http://localhost:8080/` with quick links (Swagger, Angular, React, Health, H2 console).

Sample output (Docker path):
```
[start] Launching via docker-compose (mock profile)
[start] Waiting for backend health
[start] Backend is up.
[start] URLs:
	API:    http://localhost:8080/swagger-ui.html
	Angular static (if built): http://localhost:8080/app/angular/
	React static (if built):   http://localhost:8080/app/react/
```

### 6. Verify it works
Health check:
```
curl -s http://localhost:8080/actuator/health | jq . 2>/dev/null || curl -s http://localhost:8080/actuator/health
```
Swagger UI: open http://localhost:8080/swagger-ui.html in a browser.

List seeded movies (DTO list):
```
curl -s http://localhost:8080/api-soundtracker/db-movie/all-movies-dto | jq '.[0]'
```
Legacy vs corrected API prefix:
The backend historically exposed endpoints under the misspelled prefix `/api-soudtracker/` (missing the first 'n'). A transitional change now allows BOTH `/api-soudtracker/**` and the corrected `/api-soundtracker/**` forms. Frontend code and examples in this README use the corrected spelling. Old clients or cached frontend bundles using the misspelled prefix will continue to function until the legacy mapping is removed in a future cleanup. Plan: validate all consumers have migrated, then delete the legacy prefix.

Example (truncated) JSON you might see:
```
{
	"id": 1001,
	"title": "Demo Movie",
	"year": 2024,
	"genres": ["Sci-Fi"],
	...
}
```
Static frontend (if built by setup):
- Angular: http://localhost:8080/app/angular/
- React:   http://localhost:8080/app/react/

SPA clean URLs & 404 notes:
1. A lightweight controller forwards `/app/angular/` and `/app/react/` to their respective `index.html` so you can omit the filename.
2. If Angular still 404s, confirm `backend/src/main/resources/static/app/angular/index.html` exists (should be produced by setup).
3. React may 404 on first run because the build previously failed (missing dependency). Fix by:
	```bash
	cd react-client
	npm install react-modal --save
	npm run build
	cd ..
	bash scripts/setup.sh --force   # re-embeds build into backend
	```
	After success you should have `backend/src/main/resources/static/app/react/index.html` and the link will work.
4. Deep links inside the SPAs (e.g. `/app/angular/some/route`) are not yet globally forwarded. If you add client-side routing, add a catch-all controller mapping those paths back to the SPA `index.html`.

Angular base href:
The Angular app is hosted under a subpath (`/app/angular/`). To ensure its relative asset URLs work, the build now runs with `--base-href /app/angular/` and the embedded `index.html` has `<base href="/app/angular/">`. If you manually invoke Angular builds outside the setup script, reproduce this with:
```
cd angular-client
npx ng build --base-href /app/angular/
cd ..
bash scripts/setup.sh --force
```
Without the correct base-href you may see a blank (green) screen due to CSS/JS 404s.

Tracking policy for built frontends:
The generated Angular and React `index.html` (and their hashed JS/CSS bundles) are intentionally NOT tracked in Git. Only the root landing page (`static/index.html`) lives in the repo. Always run `bash scripts/setup.sh` after cloning (or `--force` after frontend source changes) to regenerate `/app/angular/` and `/app/react/` assets. This avoids noisy commits every time a hash changes while keeping the repo lightweight.

### H2 Console (Local Profile)
If you click the `H2 Console` link and press `Test Connection` without changes you may see an error about a missing `~/test` database. That's just the console's built-in default URL (`jdbc:h2:~/test`) and **not** what the app uses.

Current local profile datasource (from `application-local.properties`):
```
jdbc:h2:file:./.local/h2db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
```
To connect via the web console:
1. Replace the JDBC URL field with the exact line above.
2. User Name: `sa`
3. Password: (leave empty)
4. Click Connect.

Optional (ephemeral in‑memory DB): If you prefer a throwaway database per run, change the property to:
```
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL
```
Then use `jdbc:h2:mem:testdb` in the console. (Do this only if you don't need file persistence between runs.)

Security note: The console is enabled and unauthenticated only in local usage scenarios. Do **not** expose it in production; disable by removing `spring.h2.console.enabled=true` or restricting the `/h2-console/**` path.

---

### 7. Prove offline capability
1. Stop everything: `docker compose down` (if using Docker) OR Ctrl+C for local run.
2. Disable network (airplane mode / unplug / turn off Wi-Fi).
3. Run `bash scripts/start.sh` (or `scripts\start.ps1`).
4. Health & movie endpoints should still respond because all dependencies & dataset are local.

### 8. Common next actions
- Extend dataset (see section: Extending the Offline Dataset).
- Rebuild frontends after making UI changes: re-run setup with `--force`.
- Switch to H2 even if Docker exists: run `SPRING_PROFILES_ACTIVE=local ./backend/mvnw spring-boot:run` (advanced / optional).

## How the Two Commands Work
| Command | Core Responsibilities | Idempotency Mechanism |
|---------|-----------------------|------------------------|
| setup   | Cache Maven deps, build backend, build frontends, embed static assets, pre-pull Docker images | Signature file `.setup-complete` with hashes of `pom.xml` & each frontend `package.json` |
| start   | Prefer Docker (Postgres + mock profile), wait for health; else local Spring Boot with H2 | Detects `.setup-complete`; falls back seamlessly if Docker absent |

Implementation notes:
- Frontend embedding means no separate Node server containers are required.
- Adds reproducibility: the backend serves a frozen dist of the UIs.
- Re-running setup after only source edits (no dependency changes) is optional unless you changed frontend code that needs a rebuild.

## Extending the Offline Dataset
Movie fixtures live in: `backend/src/main/resources/data/movies/`
Music (album) fixtures: `backend/src/main/resources/data/music/albums/`

Add a new movie:
1. Create a JSON file named with a numeric ID, e.g. `1050.json`.
2. Minimal shape (example):
	 ```json
	 {
		 "id": 1050,
		 "title": "Another Demo Movie",
		 "year": 2022,
		 "genres": ["Action", "Drama"],
		 "description": "Short synopsis here"
	 }
	 ```
3. Restart the application (no need to re-run setup unless you also added dependencies). The seeder will ingest the new fixture if that ID is not already in the database.

To update a fixture that was already seeded:
- Delete it from Postgres (if using Docker) or from the H2 database, or clear the DB (see Cleanup & Reset) then restart.

Albums association:
- The mock seeder may attempt simple title heuristics; ensure album fixture titles align with the movie if you want automatic linkage.

## Troubleshooting Matrix (Condensed)
| Symptom | Cause | Fix |
|---------|-------|-----|
| `permission denied: ./scripts/setup.sh` | Missing execute bit | `chmod +x scripts/*.sh backend/mvnw` or `bash scripts/setup.sh` |
| PowerShell: `script cannot be loaded` | Execution policy blocks local scripts | `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` |
| `-Dmaven.multiModuleProjectDirectory system property is not set.` | Non-standard wrapper / line endings | Use included wrapper; ensure `dos2unix backend/mvnw`; add execute bit |
| Port 8080 already in use | Another process/container running | Stop conflict (`lsof -i:8080`), or set `server.port` in a profile override |
| Docker not installed/running | Start script can't launch containers | Start script falls back automatically to H2 local run (message logged) |
| First run offline fails (dependency not found) | Dependencies not cached yet | Run `setup` once while online |
| Node build OOM / slow | Low RAM / many parallel processes | Re-run with `npm install --no-audit`; close other apps; optionally skip frontend build if not needed |
| Changed fixture not reflected | Old row persisted | Delete row from DB or wipe volume (see Cleanup & Reset) then restart |
| InvalidConfigDataPropertyException referencing spring.profiles.active | `spring.profiles.active` declared inside `application-<profile>.properties` | Remove that line; activate profile via `SPRING_PROFILES_ACTIVE` env or command arg only |

## Cleanup & Reset
Docker path (remove containers & volumes):
```
docker compose down -v
```
Remove the setup signature so next run does a full rebuild:
```
rm -f .setup-complete               # macOS/Linux
Remove-Item .setup-complete -ErrorAction SilentlyContinue  # Windows
```
Force a full rebuild (dependencies + frontends):
```
bash scripts/setup.sh --force   # or scripts\setup.ps1 -Force
```
Reset only the database contents:
```
docker compose down -v && docker compose up -d   # forces new Postgres volume & reseed
```
Local run (H2) reset: just stop the app and delete the old `backend/target` folder if desired; seed runs fresh each startup for missing IDs.

Do NOT commit `.setup-complete` (it is already ignored) – it is environment-specific.

---

### Troubleshooting
Maven wrapper error `-Dmaven.multiModuleProjectDirectory system property is not set.`

Cause: A minimal/custom `mvnw` script missing the standard property pass-through.

Fix already applied in this repo. If you still encounter it:
```
cd backend
./mvnw -v
```
Should print Maven version without the error. If not, ensure execute bit and no CRLF line endings:
```
dos2unix backend/mvnw  # if installed
chmod +x backend/mvnw
```

Permission denied (macOS/Linux):
```
zsh: permission denied: ./scripts/setup.sh
```
Causes: file execute bit not set (common after some archive downloads or if Git didn't preserve mode).

Fix options:
1. Add execute permission (preferred):
```
chmod +x scripts/*.sh
./scripts/setup.sh
```
2. Or explicitly invoke with bash without changing permissions:
```
bash scripts/setup.sh
```
3. Persist executable bit in Git (if you have commit rights) so collaborators don’t hit this again:
```
git update-index --chmod=+x scripts/setup.sh scripts/start.sh backend/mvnw
git commit -m "chore: perms: make scripts & mvnw executable"
```
4. If Git keeps losing execute bits (e.g., on some network filesystems), ensure `core.fileMode` is enabled:
```
git config core.fileMode true
```
	 If you need to enforce this for everyone, add to `.gitconfig` globally or document in contribution guidelines.

Verify after fix:
```
ls -l scripts/setup.sh backend/mvnw
# Expect: -rwxr-xr-x (or similar with x bits present)
```

Windows notes:
- PowerShell scripts (`.ps1`) do not use UNIX execute bits.
- If using WSL, apply the chmod commands inside the WSL filesystem; cloning into a Windows NTFS mount can sometimes strip mode bits.

CI/CD hint:
Add a lightweight check to prevent regressions (GitHub Actions example):
```
if [[ ! -x scripts/setup.sh || ! -x scripts/start.sh ]]; then
	echo "Scripts missing execute bit" >&2; exit 1; fi
```

If running on macOS and you get a Gatekeeper prompt for an unsigned binary, this does not apply here (the scripts are plain text). Ensure your shell is not aliasing `bash` to something unexpected.

Node / npm permission errors:
Run with `npm ci` default; if you see EACCES issues, ensure you don’t use a globally restricted prefix or run inside a directory with proper ownership.

Maven offline errors:
If the first run is completely offline, Maven cannot fetch dependencies. Run `./scripts/setup.sh` once while online, then subsequent runs can be offline.

### Profiles & Secrets

| Profile | External API Calls | JWT Signing Key Source | Database | Purpose |
|---------|--------------------|------------------------|----------|---------|
| mock (default in Docker) | Stubbed (no real Kinopoisk / Spotify) | Built‑in dev key in `application-mock.properties` | Postgres container | Deterministic offline demo with container DB |
| local | Stubbed (no real Kinopoisk / Spotify) | Built‑in dev key in `application-local.properties` | Embedded H2 | Easiest zero‑Docker local run |
| live | Real (Kinopoisk / Spotify beans activated) | MUST provide via env/property (`token.signing.key`) | Depends on how you launch (Postgres or other) | Real integration testing |

Key points:
1. The dev JWT signing key embedded in `mock` and `local` is ONLY for development/offline use. Do NOT reuse in production.
2. When running with `live` profile you must supply your own strong signing key and the external API credentials; the stub beans are not loaded.
3. If you do not set the required live credentials the application context will fail to start when the `live` profile is active.

Supplying secrets for `live` (examples):

macOS / Linux (temporary shell env):
```
export TOKEN_SIGNING_KEY="$(openssl rand -base64 48)"
export KINOPOISK_API_KEY="<your-kinopoisk-key>"
export SPOTIFY_CLIENT_ID="<your-spotify-client-id>"
export SPOTIFY_CLIENT_SECRET="<your-spotify-client-secret>"
SPRING_PROFILES_ACTIVE=live ./backend/mvnw spring-boot:run
```

Windows (PowerShell):
```
$env:TOKEN_SIGNING_KEY = [Convert]::ToBase64String((New-Object byte[] 48 | %{ (Get-Random -Maximum 256) }))
$env:KINOPOISK_API_KEY = "<your-kinopoisk-key>"
$env:SPOTIFY_CLIENT_ID = "<your-spotify-client-id>"
$env:SPOTIFY_CLIENT_SECRET = "<your-spotify-client-secret>"
$env:SPRING_PROFILES_ACTIVE = "live"
./backend/mvnw spring-boot:run
```

Property name mapping:
- `token.signing.key` ⇢ env var `TOKEN_SIGNING_KEY`
- `kinopoisk.apiKey` ⇢ env var `KINOPOISK_API_KEY`
- `spotify.client.id` ⇢ env var `SPOTIFY_CLIENT_ID`
- `spotify.client.secret` ⇢ env var `SPOTIFY_CLIENT_SECRET`

Generating a strong key (alternative commands):
```
openssl rand -base64 48
``` 
or
```
dd if=/dev/urandom bs=64 count=1 2>/dev/null | base64
```

Endpoint behavior by profile:
- In `mock` / `local`, external API endpoints return deterministic stub JSON (or minimal placeholders) – suitable for UI and integration flows without network.
- In `live`, real upstream calls are performed; failures will propagate as runtime errors/logged exceptions if credentials or network are absent.

Adding more fixture JSON files under `backend/src/main/resources/data/movies` and `.../data/music/albums` then restarting extends the deterministic dataset (all profiles still seed from fixtures when entries are missing).

---

**Original external references (Spotify / Kinopoisk) are now optional and not used in default mock mode.**

| Stage                                | Description                                                                                                        | Expected (hours) | Actual (hours) |
|--------------------------------------|--------------------------------------------------------------------------------------------------------------------|------------------|----------------|
| Database                             | Design a database schema according to the project theme. Minimum 7 tables, many to many relationship is mandatory. | 6                | 6              |
| JDBC                                 | Create a console application to perform CRUD operations using JDBC.                                                | 7                | 7              |
| JSP                                  | Developing the client side using JSP.                                                                              | 20               | 17             |
| Backend using SpringBoot + Hibernate | Creating an application backend using SpringBoot and Hibernate.                                                    | 35               | 35             |
| Angular - Frontend                   | Frontend development using Angular.                                                                                | 20               | 15             |
| React - Frontend                     | Frontend development using React.                                                                                  | 15               | 7              |

## Task 1. Database architecture design

### Results: [DDL](Database/version_03_03_2024.txt)
![](https://github.com/gabrpavel/Soundtracker/blob/9c7cbecbcf5c547f7a6cac1132ed8d4b32544784/Database/ERD.png)

---

## Task 2. JDBC console application

### Results: [CODE](JDBC)
![](https://github.com/gabrpavel/website/blob/def71f9ba20d7f46a0288fbffbfcd0a0c14f0a81/SonarCloude%20Summary/JDBC.png)

---

## Task 3. Java EE. Client side using JSP (Java Server Pages)

### Results: [CODE](JSP)

### `localhost:8080/api/`

![](https://github.com/gabrpavel/website/blob/3b470a5c45cbcbe560714cad2441cfebace1e388/images/jsp/api.png)

### `localhost:8080/api/movies.jsp`

![](https://github.com/gabrpavel/website/blob/3b470a5c45cbcbe560714cad2441cfebace1e388/images/jsp/movies.png)

### `http://localhost:8080/api/movie.jsp?id=725190`

![](https://github.com/gabrpavel/website/blob/3b470a5c45cbcbe560714cad2441cfebace1e388/images/jsp/movie.png)

---

## Task 4. Backend using SpringBoot + Hibernate

### Results: [CODE](backend)

## Main features:
1. [x] Interaction with Spotify API and Kinopoisk API
2. [x] Authentication and authorization using JWT
3. [x] CRUD operations 
4. [x] Many to many relationship
5. [x] Pagination

## API Endpoints:
1. `/api/auth/sign-up` - sign up
2. `/api/auth/sign-in` - sign in
3. `/api-soundtracker/movie/info?id={id}` - get movie info by id
4. `/api-soundtracker/movie/set-album?id={id}` - set album to movie
5. `/api-soundtracker/movie/update?id={id}` - update movie info from Kinopoisk API
6. `/api-soundtracker/music/info?name={name}` - get album info by name
7. `/api-soundtracker/api-movie/info-by-title?title={title}` - get movie info by title from Kinopoisk API
8. `/api-soundtracker/api-movie/info?id={id}` - get movie info by id from Kinopoisk API
9. `/api-soundtracker/api-music/album?name={name}` - get album info by name from Spotify API
10. `/api-soundtracker/db-movie/update?id={id}` - update movie info from database
11. `/api-soundtracker/db-movie/save` - save movie to database
12. `/api-soundtracker/db-movie/delete?id={id}` - delete movie from database
13. `/api-soundtracker/db-movie/info?id={id}` - get movie info by id from database
14. `/api-soundtracker/db-movie/all-movies` - get all movies from database
15. `/api-soundtracker/db-movie/all-movies-dto` - get all movies from database with DTO
16. `/api-soundtracker/db-music/update?id={id}` - update album info from database
17. `/api-soundtracker/db-music/save` - save album to database
18. `/api-soundtracker/db-music/delete?id={id}` - delete album from database
19. `/api-soundtracker/db-music/album-by-name?name={name}` - get album info by name from database
20. `/api-soundtracker/db-music/album-by-id?id={id}` - get album info by id from database
 

---

## Task 5. Frontend using Angular

### Results: [CODE](angular-client)

![](images/angular-client/1.png)
![](images/angular-client/2.png)
![](images/angular-client/3.png)
![](images/angular-client/4.png)
![](images/angular-client/5.png)
![](images/angular-client/6.png)
![](images/angular-client/7.png)

---

## Task 6. Frontend using React

### Results: [CODE](react-client)

![](images/react-client/1.png)
![](images/react-client/2.png)
![](images/react-client/3.png)
![](images/react-client/4.png)
![](images/react-client/5.png)
![](images/react-client/6.png)
