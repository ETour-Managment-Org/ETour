# Dockerizing ETour

This kit adds Docker support to the existing project without touching any
application code. Drop these files into your project so the layout looks
like this:

```
E_Tour_Project/
├── docker-compose.yml          <- new
├── .env.example                <- new (copy to .env)
├── E-Tour/
│   ├── Dockerfile               <- new
│   ├── .dockerignore            <- new
│   └── ... (existing backend source, untouched)
└── E-tour-FrontEnd/
    ├── Dockerfile                <- new
    ├── nginx.conf                <- new
    ├── .dockerignore              <- new
    └── ... (existing frontend source, untouched)
```

## What each piece does

| File | Purpose |
|---|---|
| `E-Tour/Dockerfile` | Multi-stage build: compiles the Spring Boot jar with Maven, then runs it on a slim JRE. Ships the `images/` folder baked in; a named volume is layered on top so uploads persist. |
| `E-tour-FrontEnd/Dockerfile` | Multi-stage build: `npm run build`s the Vite app, then serves the static output with nginx. |
| `E-tour-FrontEnd/nginx.conf` | Serves the React SPA and reverse-proxies `/api/**` and `/images/**` to the backend container — this is why no CORS changes were needed; from the browser's point of view everything is same-origin. |
| `docker-compose.yml` | Wires up three services: `db` (MySQL 8), `backend`, `frontend`, on one Docker network. |
| `.env.example` | Copy to `.env` to override DB credentials, the JWT secret, and the published frontend port. |

## Why no code changes were required

- **Datasource**: `application.properties` hardcodes `jdbc:mysql://localhost:3306/...` for local dev, but Spring Boot's relaxed environment-variable binding means `SPRING_DATASOURCE_URL` (set in `docker-compose.yml`) overrides it automatically inside the container — no need to edit the properties file.
- **JWT secret**: already reads from `${JWT_SECRET:...}`, so `docker-compose.yml` just supplies that env var.
- **Images directory**: `etour.images.dir` defaults to a relative `images` folder resolved against the working directory — the Dockerfile sets `WORKDIR /app` and copies `images/` there, so it resolves correctly with zero config.
- **Frontend API base URL**: `VITE_API_BASE_URL` already defaults to the relative path `/api` (see `.env`/`.env.example` in the frontend), which is exactly what a same-origin nginx reverse proxy needs.

## Running it

```bash
cd E_Tour_Project
cp .env.example .env        # optional — defaults work out of the box
docker compose up --build
```

First run will take a few minutes (Maven downloads dependencies, npm installs
packages). Subsequent runs are much faster thanks to Docker layer caching.

- Frontend: **http://localhost** (port 80, or `FRONTEND_PORT` from `.env`)
- Backend API directly: **http://localhost:8080/api/...** (useful for Postman)
- MySQL: **localhost:3306** (root / root by default) if you want to connect a DB client

Stop everything:

```bash
docker compose down
```

Stop and wipe the database + uploaded images too:

```bash
docker compose down -v
```

## Notes / things worth knowing

- **Startup order**: `backend` waits for MySQL's healthcheck (`mysqladmin ping`) before starting, not just for the container to exist — avoids the classic "backend crashes because MySQL wasn't ready yet" race.
- **`spring.jpa.hibernate.ddl-auto=update`** means the schema is created/updated automatically on backend startup — no manual migration step needed for a fresh `db` volume.
- **`data.sql`** seeds roles and reference data on every startup (`spring.sql.init.mode=always`). This is unchanged from the original app behavior — just flagging it in case you see it re-run on every `docker compose up`.
- **Production hardening you'll want before a real deployment** (deliberately left out of this kit since it's for local/dev use): a non-default `JWT_SECRET` and DB password via `.env` (not committed to git), HTTPS termination in front of nginx, and probably moving `spring.jpa.hibernate.ddl-auto` to `validate` once you introduce a real migration tool (Flyway/Liquibase).
