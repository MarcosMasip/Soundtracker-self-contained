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
./scripts/setup.sh
./scripts/start.sh
```

Windows (PowerShell):
```
scripts\setup.ps1
scripts\start.ps1
```

### Profiles
- `mock` (default in Docker): Uses fixtures + Postgres container.
- `local`: Same fixtures, uses embedded H2 (no Docker required).
- `live`: Placeholder for future real external API integration.

Add more fixture JSON files under `backend/src/main/resources/data/movies` and `.../data/music/albums` and restart to extend dataset.

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
