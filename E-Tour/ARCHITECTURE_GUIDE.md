---
title: "e-Tour Management System"
subtitle: "Architecture and Implementation Guide"
author: "Aditya Mali — C-DAC / VITA Capstone Project"
date: "August 2026"
toc: true
toc-depth: 2
numbersections: true
---

\newpage

# How to read this document

This guide describes the **e-Tour Management System** exactly as it is built. Every
mechanism described here corresponds to code in one of three folders:

| Folder | Contents |
|---|---|
| `E-Tour` | Spring Boot 4.1 backend (Java 17) — the primary backend |
| `ETour-DotNet` | ASP.NET Core 10 backend — a feature-parity alternative |
| `E-tour-FrontEnd` | React 18 + Vite single-page application |

**A note on scope.** Three topics commonly expected in a document like this are
**not implemented** in this project: a Circuit Breaker, a standby payment
microservice, and server-side PDF generation. Rather than describe them as if
they existed, Section 8 sets out honestly what the system does instead, why that
choice was made, and what adding the real pattern would involve. An examiner who
opens the repository will find the document and the code agree — which is worth
considerably more than a longer feature list.

The system is a **modular monolith**, not a microservice architecture. It is one
deployable unit per backend, internally separated into controller, service and
repository layers. This is stated plainly throughout.

\newpage

# Dockerization Architecture

## The two files, and why both exist

`Dockerfile` and `docker-compose.yml` are often conflated. They answer different
questions.

> **A `Dockerfile` answers: "how do I turn this source code into one runnable image?"**
>
> **A `docker-compose.yml` answers: "how do several of those images run together as a system?"**

A `Dockerfile` knows nothing about databases, ports on the host, or other
services. It is a build recipe. `docker-compose.yml` never compiles anything
itself — it points at Dockerfiles, then wires the resulting containers into a
network, injects configuration, and declares start-up order.

The project has **three** Dockerfiles and **one** compose file.

## The Java Dockerfile — a multi-stage build

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd -r etour && useradd -r -g etour etour
COPY images ./images
RUN mkdir -p /app/logs
COPY --from=build /build/target/*.jar app.jar
RUN chown -R etour:etour /app
USER etour
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/api/categories/roots >/dev/null || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Four decisions in that file are worth being able to defend:

**1. Two stages.** The first stage carries the full Maven toolchain — roughly
600 MB of compiler, plugins and dependency cache. The second stage starts from a
JRE-only base and copies **just the built jar** across with
`COPY --from=build`. The Maven layer never ships. The final image is a fraction
of the size, and nothing in production has a compiler on it.

**2. `pom.xml` is copied before `src`.** Docker caches each layer and
invalidates every layer after the first change. Dependencies change rarely;
source changes constantly. Copying `pom.xml` first and running
`dependency:go-offline` means an ordinary code edit reuses the cached dependency
layer instead of re-downloading the internet.

**3. A non-root user.** `USER etour` means a compromise inside the container
does not hand over root. `mkdir -p /app/logs` runs **before** `chown`, so the
log directory is owned by `etour` — without that ordering the application starts
and then cannot write its own log file.

**4. A `HEALTHCHECK`.** Docker distinguishes "the process is running" from "the
application is actually serving requests". Hitting a real endpoint proves the
Spring context started and the database connection pool came up.

## The frontend Dockerfile — build tool out, web server in

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
ARG VITE_API_BASE_URL=/api
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
```

React is not a runtime here. `npm run build` produces a folder of static HTML,
CSS and JavaScript, and the second stage serves that folder with nginx. Node is
absent from the shipped image entirely.

`npm ci` rather than `npm install`: `ci` installs strictly from
`package-lock.json` and fails if the lock file disagrees with `package.json`. A
build should be reproducible, not "whatever was newest that afternoon".

## docker-compose.yml — the four services

| Service | Image | Host port | Container port | Depends on |
|---|---|---|---|---|
| `db` | `mysql:8.0` | 3308 | 3306 | — |
| `java-backend` | built from `E-Tour` | 8080 | 8080 | `db` |
| `dotnet-backend` | built from `ETour-DotNet` | 5000 | 5000 | `db` |
| `frontend` | built from `E-tour-FrontEnd` | 5173 | 80 | both backends |

**Service names are hostnames.** Compose creates a private network and registers
each service under its own name. The Java backend reaches the database at
`jdbc:mysql://db:3306/etour` — not `localhost`, which inside a container means
*that container*. This is the single most common Docker networking mistake, and
it produces a connection error that names two addresses (IPv4 and IPv6 loopback)
rather than one — a useful diagnostic signature.

**Published ports are for humans only.** `3308:3306` lets a developer point
MySQL Workbench at port 3308 on their laptop. The backends never use it; they
talk over the internal network on 3306.

**`depends_on` controls start order, not readiness.** It guarantees the database
*container* starts first, not that MySQL has finished initialising. This is why
the backends must tolerate a database that is not yet accepting connections.

## Volumes: what they are and why this project needs two

A container's filesystem is **ephemeral**. Remove the container and every byte
written inside it is gone. That is normally a feature — it is what makes
containers reproducible — but some data must outlive the container. A **volume**
is storage managed by Docker and mounted into the container at a chosen path,
whose lifetime is independent of any container.

Three distinct kinds are used here:

**Named volume — `etour_db_data:/var/lib/mysql`**

: MySQL writes its data files to `/var/lib/mysql`. Without this line, every
  `docker compose down` would destroy the entire database. Docker manages the
  storage location; the developer never touches it directly.

**Named volume — `etour_images:/app/images`**

: Tour photographs uploaded through the admin panel. These are user-generated
  content that was never in the source tree, so a rebuild of the image would
  otherwise lose them.

**Bind mount — `./mysql/data.sql:/docker-entrypoint-initdb.d/data.sql`**

: A bind mount maps a *specific host path* into the container. The MySQL image
  runs any `.sql` file placed in `/docker-entrypoint-initdb.d` — **but only on
  first initialisation of an empty data directory.** Once `etour_db_data` holds
  a database, this script is ignored. Re-seeding therefore requires
  `docker compose down -v` to drop the volume.

**Bind mount — `./E-tour/logs:/app/logs`**

: Makes the application's log files readable from the host. This was added to
  solve a real problem: log files on the host appeared frozen for days. The
  application was writing correctly to `/app/logs` *inside* the container, and
  the host file was a stale leftover from before dockerization. No mount, no
  visibility. A `@PostConstruct` hook in the logging aspect now prints the
  resolved absolute path at startup so the question can never be ambiguous
  again.

> **Named volume vs bind mount.** A named volume is managed by Docker and is the
> right choice for data whose location does not matter (databases, uploads). A
> bind mount points at a specific host directory and is the right choice when a
> human needs to read or write those exact files.

## nginx as the single front door

The frontend container serves the built React app *and* proxies every API call:

```nginx
upstream app_backend {
    server java-backend:8080;
    #server dotnet-backend:5000;
}
```

`/api/`, `/images/` and the OAuth callback paths are all proxied to
`app_backend`. Two consequences:

1. **The browser only ever talks to one origin**, so the API is same-origin and
   CORS never applies in the deployed configuration.
2. **Switching backends is a one-line change.** Comment one server line,
   uncomment the other, restart the frontend container. The React code contains
   no backend URLs at all.

An earlier revision proxied `/api/` to Java but the Google callback to .NET.
That "split brain" would have authenticated a user against one backend and then
issued requests to another. Routing everything through a single named upstream
makes that class of mistake structurally impossible.

\newpage

# Aspect-Oriented Programming

## The problem AOP solves

Some requirements are not features. Logging, timing, auditing and transaction
management are needed in dozens of methods but belong to none of them. Written
inline, they scatter identical boilerplate through every service and bury the
business logic underneath it. These are **cross-cutting concerns**.

AOP extracts that logic into an **aspect** — a separate class that declares
*where* it applies (a **pointcut**) and *what* it does (**advice**). The
business method stays clean and has no idea it is being observed.

Spring implements this with **proxies**. When a bean matches a pointcut, Spring
wraps it in a generated proxy; callers get the proxy, which runs the advice
before delegating to the real object.

> **The consequence to remember:** because it is proxy-based, a method calling
> another method *on itself* bypasses the proxy entirely, and the advice does
> not run. Self-invocation is the classic reason an aspect "silently does
> nothing".

The project uses `spring-boot-starter-aspectj`. In Spring Boot 4 this starter
was renamed from `spring-boot-starter-aop`.

## Three aspects, three jobs

### RequestContextAspect — who is doing this?

Runs first (`@Order(1)`) on every controller method. It puts a correlation id,
the username, the client IP and the endpoint into **MDC** (Mapped Diagnostic
Context), a thread-local map that Logback interpolates into every log line. One
request id then ties together every line that request produced.

```java
@Around("within(com.etour.controllers..*)")
public Object addContext(ProceedingJoinPoint pjp) throws Throwable {
    MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
    MDC.put("user", currentUsername());
    try {
        return pjp.proceed();
    } finally {
        MDC.clear();
    }
}
```

**`MDC.clear()` in a `finally` block is not optional.** MDC is thread-local and
Tomcat pools its threads. A thread that finishes a request keeps its MDC values
and is handed to the next request — so one user's identity would be stamped onto
another user's log lines. In an audit log that is not untidy, it is wrong.

### UserActivityAspect — the audit trail

Wraps seven groups of business actions: login, registration, place booking,
cancel booking, change/reset password, admin catalogue changes and review
submission. Each writes one line to a dedicated activity log — timestamp, user,
action, target, and `SUCCESS` or `FAILED` — then **rethrows** any exception so
behaviour is unchanged.

```java
@Around("loginPointcut() || bookingPointcut() || ...")
public Object audit(ProceedingJoinPoint pjp) throws Throwable {
    try {
        Object result = pjp.proceed();
        activityLog.info("... | SUCCESS");
        return result;
    } catch (Throwable t) {
        activityLog.info("... | FAILED: " + t.getMessage());
        throw t;                       // never swallow
    }
}
```

**A genuine bug worth mentioning in a viva:** one pointcut originally named
`ReviewService.createReview`. The actual method is `submitReview`. Pointcuts are
**strings** — they are not checked by the compiler. The application compiled,
started, and silently audited nothing for that action. A `@PostConstruct` hook
now logs which action groups are active and the absolute path of the log file,
so a mis-typed pointcut is visible on the first line of every boot.

### PerformanceAspect — what is slow?

`@Order(2)`, applied to the service and repository layers. Times each call and
logs at `WARN` above a configurable threshold (`etour.logging.slow-ms`,
default 400 ms).

The repository pointcut uses `execution(* com.etour.repositories..*.*(..))`
rather than `within(...)`. Spring Data repositories are **interfaces**
implemented by runtime proxies; there is no class in that package for `within`
to match, so the more obvious form matches nothing.

## Transaction management is AOP too

`@Transactional` is the aspect everyone uses without noticing. Spring wraps the
annotated method: open a transaction before, commit after, roll back on a
runtime exception. `BookingServiceImpl.placeBooking` carries it, which is what
makes booking creation atomic — Section 7 covers what that guarantees.

Because it is the same proxy mechanism, `@Transactional` inherits the same
caveat: a private method, or a method called from within the same class, is not
advised.

## Logging: SLF4J and Logback, not Log4j

A frequent confusion worth stating precisely:

- **SLF4J** is a *façade* — an API of interfaces (`Logger`, `LoggerFactory`).
  Application code compiles against this and nothing else.
- **Logback** is the *implementation* that actually formats and writes lines. It
  is Spring Boot's default and is configured here in `logback-spring.xml`.
- **Log4j2** is a *competing implementation*. It is **not** used in this
  project.

The value of the façade is that the implementation can be swapped without
touching a single line of application code.

\newpage

# Authentication: OAuth 2.0 and JWT

## Two mechanisms answering two different questions

| | Question answered | Used for |
|---|---|---|
| **OAuth 2.0** | "Is this person who they claim to be?" — delegated to Google | Sign-in with Google |
| **JWT** | "Is this request from someone already authenticated?" | Every subsequent API call |

OAuth runs **once**, at sign-in. JWT is what the browser presents on **every**
request afterwards. Both paths — password login and Google login — converge on
the same outcome: the server issues one of our own JWTs.

## The OAuth 2.0 Authorization Code flow

```
Browser              Our backend                    Google
   |                     |                             |
   |-- click "Google" -->|                             |
   |<-- 302 redirect ----|                             |
   |------------------ consent screen ---------------->|
   |<----------------- user approves ------------------|
   |<-- 302 back with ?code=xyz -----------------------|
   |-- GET /login/oauth2/code/google -->|              |
   |                     |-- code + client_secret ---->|
   |                     |<-- id_token + profile ------|
   |                     |                             |
   |                [find or create local user]        |
   |                [mint OUR JWT]                     |
   |<-- redirect to /oauth/callback#token=... ---------|
```

Points that matter:

**The authorization code is not the credential.** It is short-lived, single-use,
and worthless on its own. It must be exchanged — server to server — together
with the **client secret**, which never reaches the browser. This is precisely
why the flow is called Authorization *Code*: the front channel carries only a
reference; the back channel carries the actual token.

**Google authenticates; it does not authorize.** Google confirms the identity.
What that identity is *allowed to do* in e-Tour is our decision, resolved by
looking up (or creating) a local `User` row and reading its role.

**A real bug from this project.** The .NET callback originally called
`AuthenticateAsync(GoogleDefaults.AuthenticationScheme)` and always failed.
Google is a *remote* handler: it completes at `/signin-google`, signs the
principal into the **cookie** scheme, then redirects. The callback must read the
cookie scheme — `CookieAuthenticationDefaults.AuthenticationScheme` — and sign
that temporary cookie out once the JWT is minted.

**Configuration guard.** Spring validates every OAuth2 registration when the
properties bean is created, and an *empty* client-id is a hard startup failure.
The properties therefore default to the literal `not-configured`, which passes
validation while the application treats it as "OAuth is off" and hides the
button.

## What a JWT actually is

Three Base64url segments joined by dots:

```
eyJhbGciOiJIUzUxMiJ9 . eyJzdWIiOiJhZG1pbiIsImV4cCI6MTc1... . 4nZ8pQ...
      HEADER                        PAYLOAD                    SIGNATURE
```

| Segment | Contents | In this project |
|---|---|---|
| **Header** | signing algorithm | `{"alg":"HS512"}` |
| **Payload** | claims | `sub` (username), `iat`, `exp` |
| **Signature** | HMAC of header + payload | keyed with the server secret |

Token creation, in full:

```java
Jwts.builder()
    .subject(username)
    .issuedAt(new Date())
    .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
    .signWith(key())
    .compact();
```

The key is derived from a Base64-encoded secret:
`Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))`. HS512 requires at
least 64 bytes of key material after decoding.

**The payload carries only the username.** Roles are deliberately *not* in the
token. They are loaded from the database on each request, so an administrator
demoted at 10:00 loses access at 10:00 — not whenever their token happens to
expire. The cost is one database read per request; the benefit is that
authorization decisions are never stale.

## Signing is not encryption

This distinction is asked about constantly, and the answer is short:

> **Signing proves a message was not altered and came from someone holding the
> key. It does not hide anything.** Anyone can Base64-decode a JWT payload and
> read it.

Our tokens are **signed (JWS), not encrypted (JWE)**. Paste one into
`jwt.io` and the username and expiry are plainly visible. That is acceptable
because the payload contains nothing secret. What an attacker *cannot* do is
change `sub` to `admin` and have it accepted — any edit invalidates the HMAC,
and producing a valid one requires the server secret.

**The practical rule: never put anything in a JWT payload you would not print on
a postcard.**

## Stateless authentication, and how the filter works

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.csrf(csrf -> csrf.disable())
.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
```

The server stores **no session**. Every request arrives with everything needed to
authorize it, which is what allows any instance to serve any request — the
property that makes horizontal scaling possible.

`AuthTokenFilter` runs once per request and:

1. reads the `Authorization` header and strips the `Bearer ` prefix;
2. verifies the signature and expiry;
3. loads the user from the database;
4. places an `Authentication` into the `SecurityContext`.

If any step fails it simply does not populate the context — it does not throw.
The request continues as anonymous, and the authorization rules decide whether
that is acceptable. This is why a bad token on a public endpoint still works.

**CSRF is disabled deliberately, and that is safe here.** CSRF attacks rely on
the browser *automatically* attaching a credential — a cookie. Our token lives
in `localStorage` and is attached by explicit JavaScript, which a cross-site
form post cannot do. Disabling CSRF protection while using cookie
authentication would be a serious vulnerability; with bearer tokens it is
correct.

## BCrypt for passwords

Passwords are hashed with BCrypt. Three properties matter:

- **One-way.** A stolen database yields no passwords.
- **Salted, automatically.** The salt is generated per password and stored
  *inside* the resulting string: `$2b$10$<22-char salt><31-char hash>`. Two
  users with the same password get different hashes, so rainbow tables are
  useless.
- **Deliberately slow.** The `10` is a work factor — 2^10 rounds. It costs a
  legitimate login about 100 ms and costs an attacker brute-forcing billions of
  guesses an unacceptable amount. Raising the number as hardware improves is a
  one-character change.

## Route protection

```java
.requestMatchers(HttpMethod.GET, "/api/tours/**").permitAll()
.requestMatchers(HttpMethod.POST,   "/api/tours/**").hasRole(RoleName.ADMIN)
.requestMatchers(HttpMethod.PUT,    "/api/tours/**").hasRole(RoleName.ADMIN)
.requestMatchers(HttpMethod.DELETE, "/api/tours/**").hasRole(RoleName.ADMIN)
.requestMatchers("/api/admin/**").hasRole(RoleName.ADMIN)
.requestMatchers("/api/bookings/**").authenticated()
.requestMatchers(HttpMethod.GET,  "/api/payments/config").permitAll()
.requestMatchers(HttpMethod.POST, "/api/payments/order").authenticated()
```

Rules are evaluated **in order, first match wins**, so specific rules must be
declared before general ones. An earlier revision matched `/api/tours/**`
without specifying a method, which made every browsing user able to `POST` a new
tour. Splitting the rules per HTTP verb closed it.

The React side has its own `ProtectedRoute` and `AdminRoute` wrappers, but those
are **user experience, not security** — they stop someone seeing a broken admin
screen. The real enforcement is the filter chain, because anyone can call the
API directly with `curl`.

\newpage

# Frontend Architecture

## The stack

React 18 with Vite 6, React Router 6, plain CSS, and the browser's own `fetch`.
No Redux, no `useReducer`, no axios, no component library.

**Why no Redux.** Redux earns its boilerplate when many unrelated components
mutate a large shared store. This application has exactly two pieces of truly
global state — who is logged in, and the booking currently being assembled.
Everything else is server data fetched by the screen that displays it. Two React
Contexts model that in a fraction of the code.

**Why `fetch` and not axios.** One thin wrapper, `api/client.js`, provides the
two things axios is usually imported for — automatic JSON handling and a single
place to attach the auth header. Adding a dependency to save twenty lines is a
poor trade.

## State management: three Contexts

```jsx
<I18nProvider>          {/* language, in main.jsx  */}
  <AuthProvider>        {/* who is logged in       */}
    <BookingProvider>   {/* the in-progress booking */}
      <AppRoutes />
    </BookingProvider>
  </AuthProvider>
</I18nProvider>
```

**`AuthContext`** holds the current user and exposes `login`, `register`,
`logout`, plus the derived flags `isLoggedIn` and `isAdmin`. It seeds itself
from `localStorage` on mount, so a page refresh does not sign the user out.
`logout` clears both the token and the cached user — an earlier version cleared
only the token, and the previous user's booking draft remained visible after
signing out.

**`BookingContext`** holds the multi-step booking draft — tour, schedule,
passengers, contact details — so navigating between the four booking steps does
not lose data. It also survives a redirect to the login page, which is what lets
an anonymous visitor start a booking, be asked to sign in, and land back on the
step they left.

**`I18nContext`** supplies `t()` for interface strings and `tc()` for
server-supplied content.

Each Context is consumed through a small custom hook — `useAuth()`,
`useBooking()`, `useI18n()` — rather than `useContext` scattered through the
components. This keeps the import surface small and means the context object can
be restructured in one place.

## Hooks in use

**`useState`** — all local component state.

**`useEffect`** — data fetching on mount, and cleanup. The pattern used
throughout guards against setting state after unmount:

```jsx
useEffect(() => {
  let alive = true
  paymentApi.config()
    .then((cfg) => { if (alive) setProvider(cfg.provider) })
    .catch(() => alive && setProvider('mock'))
  return () => { alive = false }        // cleanup
}, [])
```

Without the `alive` flag, navigating away before the response arrives sets state
on an unmounted component.

**`useMemo`** — memoises derived values such as the fare quote, which would
otherwise be recomputed on every keystroke in the passenger form.

**`useCallback`** — stabilises the handler identities returned by `useForm` so
they are not new functions on every render.

**`useParams` / `useNavigate` / `useLocation`** — routing.

**`lazy` + `Suspense`** — code splitting for the Lottie player.

## `useForm` — the one custom hook

Validation lives in three files: `validation/rules.js` (composable validators),
`validation/schemas.js` (one schema per form), and `hooks/useForm.js`, which
owns the values, the errors, and — the interesting part — **when an error is
allowed to appear**.

```js
const error = useCallback(
  (name) => ((touched[name] || submitted) ? errors[name] || null : null),
  [errors, touched, submitted]
)
```

Errors surface only after a field has been blurred or the form submitted.
Validating on every keystroke tells someone their email is invalid the moment
they type the first letter, which is technically true and genuinely unpleasant
to use. On submit, the hook marks every field touched and scrolls to the first
invalid one.

Forms carry `noValidate`. Without it the browser's own validation bubbles fire
first, producing two competing error systems — one of which cannot be styled or
translated.

Client-side validation is a **convenience, never a control**. Every rule is
enforced again server-side by Jakarta Bean Validation, because anyone can post
directly to the API.

## Rendering and code splitting

`LottieBox` lazy-loads the animation player:

```jsx
const Lottie = lazy(() => import('lottie-react'))
```

The animation JSON files are 1.6–2.2 KB each; the *player* is around 250 KB.
Importing it eagerly would put it in the main bundle and slow the home page for
an animation most visitors never see. The `Suspense` fallback is an empty box of
identical dimensions so nothing on the page jumps while the chunk downloads. The
component also honours `prefers-reduced-motion`, rendering a static first frame
for users who have asked their operating system to stop animations.

The same technique defers `xlsx` (spreadsheet import, admin only) and `jspdf`
(receipt download).

\newpage

# Backend Initialization Lifecycle

What happens between `docker compose up` and the first served request:

**1. `main()` calls `SpringApplication.run(ETourApplication.class, args)`.**

**2. Environment resolution.** Spring assembles a layered `Environment`.
Precedence, highest first: command-line arguments, OS environment variables,
`application-{profile}.yml`, `application.properties`, defaults in code. This
is how `${PAYMENT_PROVIDER:mock}` in the properties file reads the environment
variable Compose injects, and silently falls back to `mock` when it is absent.

> **A hard-won lesson recorded here:** a *misspelled* configuration key is not an
> error to Spring or to ASP.NET Core. It is an **absent** key — and absent keys
> take their default. This is exactly how OAuth and email appeared to be
> configured in Docker while being entirely inert. Nothing was logged, because
> from the framework's point of view nothing was wrong.

**3. Component scan and bean definition.** Spring scans `com.etour`, registers
every `@Component`, `@Service`, `@Repository`, `@Controller` and
`@Configuration`, and evaluates conditions. `@ConditionalOnProperty` decides
here whether `MockPaymentGatewayService` or `RazorpayPaymentGatewayService` is
registered — exactly one of them exists in the context.

**4. Auto-configuration.** Boot inspects the classpath and configures what it
finds: MySQL on the classpath plus a datasource URL produces a `DataSource` and
a HikariCP connection pool; Spring Security produces a filter chain.

**5. Bean instantiation and dependency injection.** Constructor injection
throughout, which makes dependencies explicit and fields `final`. Spring detects
circular dependencies here and fails fast.

**6. Proxy creation.** Beans matching any pointcut — the three aspects and
`@Transactional` — are wrapped. This is the moment AOP becomes real.

**7. Hibernate schema management.** `ddl-auto=update` compares the entity model
against the live schema and issues additive DDL. This is what added the
`transaction_ref` column to the `payment` table without a migration script.

> `update` never drops or narrows a column, which makes it safe for a project of
> this size but unsuitable for production, where a versioned tool such as
> Flyway or Liquibase belongs.

**8. `data.sql` seeding.** `spring.sql.init.mode=always` runs the seed script on
every start.

**9. `@PostConstruct` callbacks.** `UserActivityAspect.announce()` writes a
startup line naming the active audit groups and the resolved absolute log path.

**10. Embedded Tomcat binds port 8080.** Only now does the application serve
requests. `@Scheduled` tasks — the nightly tour-completion sweep — are armed,
and the `mailExecutor` thread pool is ready.

**11. Docker health check.** `HEALTHCHECK` polls a real endpoint until it
answers, after a 40-second grace period.

The .NET service follows the same shape with different vocabulary:
`WebApplication.CreateBuilder` → register services on `builder.Services` →
`builder.Build()` → middleware pipeline in order (exception handler,
authentication, authorization, endpoints) → `app.Run()`.

\newpage

# API Communication, End to End

## One request, followed all the way

Take the admin panel loading the tour list.

**Step 1 — the component asks.**

```jsx
useEffect(() => { adminApi.tours().then(setTours).catch(setError) }, [])
```

**Step 2 — the API module names the endpoint.** Every call lives in
`src/api/*.js`, never inline in a component. The second argument is the flag
that says this call needs authentication:

```js
tours: () => api.get('/admin/tours', true)
```

**Step 3 — the client attaches the token.** `api/client.js` is the only place in
the codebase that touches the `Authorization` header:

```js
if (auth) {
  const token = tokenStore.get()
  if (token) headers['Authorization'] = `Bearer ${token}`
}
```

**Step 4 — nginx proxies.** The browser requests `/api/admin/tours` on its own
origin. nginx matches `/api/` and forwards to `app_backend`, adding the
`X-Forwarded-*` headers so the backend can recover the real client IP for the
audit log.

**Step 5 — the security filter chain.** `AuthTokenFilter` extracts the token,
verifies the HS512 signature and the expiry, loads the user, and populates the
`SecurityContext`. Then `/api/admin/**` requires `hasRole(ADMIN)`.

**Step 6 — the aspects wrap the call.** `RequestContextAspect` stamps the
request id and user into MDC; `PerformanceAspect` starts its timer.

**Step 7 — controller, service, repository.** The controller validates the DTO
with `@Valid`, delegates to the service, which owns the business rules and the
transaction boundary, which calls the repository.

**Step 8 — entities become DTOs.** Entities are never returned directly. This
keeps the password hash out of the JSON and prevents Jackson from walking a lazy
JPA association and issuing surprise queries mid-serialization.

**Step 9 — the response comes back.** The client parses it, throwing a typed
`ApiError` on any non-2xx status.

## Token storage: the trade-off, stated honestly

The token is kept in `localStorage`.

| | `localStorage` | `httpOnly` cookie |
|---|---|---|
| Readable by JavaScript | yes | no |
| Vulnerable to XSS | **yes** | no |
| Sent automatically | no | yes |
| Vulnerable to CSRF | no | yes, needs a token |

**Neither option is strictly safer — they trade one attack for another.**
`localStorage` is exposed to cross-site scripting; an `httpOnly` cookie is
immune to XSS but is attached automatically, which is the precondition for CSRF.

This project chose `localStorage` for simplicity, and the choice is coherent:
because the credential is attached by explicit JavaScript and never
automatically, disabling CSRF protection on the server is correct rather than
negligent. A production system handling real payments should use an `httpOnly`,
`Secure`, `SameSite=Strict` cookie with a short-lived access token and a refresh
token.

**Refresh tokens are not implemented.** When the token expires the user signs in
again. This is documented rather than hidden.

## Error handling on both sides

A `@RestControllerAdvice` maps exceptions to HTTP status codes centrally:
validation failures to 400, bad credentials to 401, forbidden to 403, missing
entities to 404, business-rule conflicts to 409, and anything unexpected to 500
with a generic message — stack traces go to the log, never to the browser.

The .NET side achieves the same through `ExceptionMiddleware`, which additionally
unwraps `DbUpdateException` via `GetBaseException().Message`, because EF Core's
default message — *"An error occurred while saving the entity changes. See the
inner exception for details."* — tells the user nothing at all.

On the client, `ApiError` carries the status, the message and the parsed body,
and a `401` clears the stored token so the user is not left in a broken
half-authenticated state.

\newpage

# Payments, Bookings, Notifications and PDFs

## Payment integration

The system supports **two payment providers behind one interface**:

```java
public interface PaymentGatewayService {
    String provider();
    OrderResult createOrder(BigDecimal amount, String receipt);
    PaymentResult charge(BigDecimal amount, String method, Integer bookingRef);
    PaymentResult verify(VerificationRequest request, BigDecimal expectedAmount);
}
```

`MockPaymentGatewayService` always succeeds and never touches the network.
`RazorpayPaymentGatewayService` talks to the real Razorpay test API. Exactly one
is registered, selected by a single configuration value:

```java
@ConditionalOnProperty(name = "etour.payment.provider",
                       havingValue = "mock", matchIfMissing = true)
```

Nothing else in either backend knows which is active. `matchIfMissing = true` on
the mock means the application always starts with a working checkout even with
no keys configured.

### The Razorpay flow

```
Browser                    Our server                     Razorpay
   |-- POST /api/payments/order -->|                          |
   |                               |-- POST /v1/orders ------>|
   |<-- orderId, keyId, amount ----|<-- order_XXX ------------|
   |                                                          |
   |-- checkout modal, card entered ------------------------->|
   |<-- razorpay_order_id, payment_id, signature -------------|
   |                               |                          |
   |-- POST /api/bookings/place -->|                          |
   |     (+ the three values)      |-- HMAC check (local) --  |
   |                               |-- GET /v1/payments/{id}->|
   |<-- booking confirmed ---------|<-- captured, 500000 -----|
```

Four design decisions:

**Card details never reach our servers.** They go from the browser directly to
Razorpay's hosted modal, which keeps the application out of PCI scope entirely.

**The booking is created only after payment succeeds.** The reverse order would
leave an unpaid booking holding seats every time someone closes the modal.

**The three values from the browser are a claim, not proof.** Anyone can POST a
fabricated payment id. Razorpay signs `orderId|paymentId` with our key secret, so
the server recomputes the HMAC and compares:

```java
String expected = hmacSha256(orderId + "|" + paymentId, keySecret);
if (!constantTimeEquals(expected, signature)) reject();
```

A forged pair cannot produce a matching signature without the secret, which never
leaves the server. The comparison is constant-time rather than `equals`, because
short-circuiting on the first differing byte leaks — through timing — how many
leading characters were correct.

**The amount is re-checked independently.** The signature proves the identifiers
are genuine; it says nothing about how much money moved. The server therefore
calls `GET /v1/payments/{id}` and compares the captured amount against the total
**it** recomputed from the fare bands. This is what stops a tampered client
creating a ₹1 order and using it to book a ₹5,000 tour.

No Razorpay SDK is used. Two HTTP calls and one HMAC do not justify the
transitive dependencies, and the signature check is the part worth being able to
explain rather than point at.

### Resilience: what exists, and what does not

**Implemented — a provider switch.** Setting `PAYMENT_PROVIDER=mock` and
restarting reverts the entire system to a gateway that always succeeds and needs
no internet. This is a genuine, tested fallback, but it is a **manual,
deployment-time** switch operated by a human.

**Implemented — graceful degradation.** Every Razorpay failure path returns a
`PaymentResult(false, …)` with a customer-safe message rather than propagating an
exception. The frontend distinguishes all three checkout exits — success, card
declined, and modal dismissed — because wiring only the success handler leaves
the spinner running forever when a user presses Escape.

**Not implemented — a Circuit Breaker.** There is no Resilience4j, no
`@CircuitBreaker`, no automatic state machine. The system does not detect a
degraded gateway or trip over to a standby at runtime.

> **How it would be added, if asked.** Add `resilience4j-spring-boot3`, annotate
> `createOrder` and `verify` with `@CircuitBreaker(name = "razorpay",
> fallbackMethod = "queueForRetry")`, and configure a sliding window — for
> example, trip to OPEN after 50 % of the last 20 calls fail, stay open for 30
> seconds, then admit a few HALF_OPEN probes. The fallback would place the
> booking in `PAYMENT_PENDING` and enqueue it for reconciliation rather than
> silently confirming it. The important design point is that **a payment
> fallback must never fabricate a success** — the only safe fallback is to defer,
> because the alternative is a confirmed booking nobody paid for.

**Not implemented — a standby payment microservice.** The system is a modular
monolith. A second gateway would be a second implementation of
`PaymentGatewayService`, not a separate deployable, and the interface is
deliberately shaped so that adding one requires no changes anywhere else.

## Booking transaction flow

`placeBooking` is annotated `@Transactional`, making the whole sequence atomic:

1. Load and validate the tour and the departure schedule.
2. Check seat availability against the passenger count.
3. Compute each passenger's age **at the departure date**, not today, and assign
   a fare band.
4. Price each passenger from the band ratios — twin sharing 1.0, single 1.45,
   extra person 0.85, child with bed 0.75, child without bed 0.55.
5. Validate room and extra-bed occupancy limits.
6. Persist the booking and passengers.
7. Verify the payment (Razorpay) or charge it (mock), and persist a `Payment` row
   with the gateway's transaction reference.
8. If payment failed, **throw** — the transaction rolls back and no booking
   survives.
9. Set the status to `CONFIRMED` and decrement the seat count.
10. Trigger the invoice email.

Steps 6 to 9 either all happen or none do. The failure case matters most: an
exception at step 8 rolls back the booking *and* the seat decrement, so a failed
payment cannot leave seats reserved.

**The fare band is stored, not recomputed.** A passenger who was a child at the
time of booking must still appear as a child on a receipt reprinted two years
later. Recomputing from the birth date would silently rewrite history.

## Asynchronous notifications

Five events send email: welcome on registration, password changed, booking
invoice, cancellation with the refund figure, and tour completed.

Every one is `@Async` on a dedicated pool:

```java
@Bean(name = "mailExecutor")
public Executor mailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("etour-mail-");
    executor.initialize();
    return executor;
}
```

**Why asynchronous.** An SMTP handshake takes one to three seconds. Sending
inline would make the user wait for the mail server before seeing their
confirmation, and — worse — an SMTP outage would fail the booking request for a
booking that was already paid for and committed. Handing the work to a separate
pool means email failure is a logged warning, not a lost booking.

**A dedicated executor, not the common pool.** A slow mail server can saturate
its own five threads without touching request handling.

**Recipient precedence.** The invoice goes to the contact email entered on the
booking form if present, otherwise the account email. Someone booking on behalf
of a relative should be able to send the ticket to that relative.

`MAIL_OVERRIDE` redirects every message to one address for testing, and
`MAIL_ENABLED=false` disables sending entirely while still logging what *would*
have been sent — useful for demonstrations.

## PDF generation — where it actually happens

**PDFs are generated in the browser, not on the server.** The receipt is built
with **jsPDF** in `src/utils/receiptPdf.js` and saved directly with
`doc.save()`.

```js
export async function downloadReceipt(booking, { bandLabel } = {}) {
  const { jsPDF } = await import('jspdf')      // lazily loaded
  const doc = new jsPDF({ unit: 'pt', format: 'a4' })
  // header band, key facts, passenger table, totals, footer
  doc.save(`etour-receipt-${ref}.pdf`)
}
```

**Why client-side.** The receipt is rendered entirely from data the page already
holds, so a round trip would buy nothing. It costs the server no CPU and no
memory, and it works offline once the page has loaded. jsPDF is lazily imported,
so its ~350 KB is downloaded only by the people who press the button.

**What this replaced.** The button originally called `window.print()`, which
does not download anything — it opens the operating system print dialog and
leaves the user to find "Save as PDF" in it. On iOS Safari that option does not
exist, so the button did nothing at all. This is worth mentioning because it is
a good example of a feature that appears to work in testing and fails for a
whole class of users.

**Not implemented — server-side generation.** There is no Puppeteer, iText,
PDFBox or OpenPDF in this project.

> **When server-side generation is the right choice, and how it would work.**
> Move to the server when the document must be *identical* for every user,
> archived, signed, or emailed as an attachment — none of which the browser can
> guarantee. The implementation would be a controller returning
> `ResponseEntity<byte[]>` (or better, a `StreamingResponseBody` so a large
> document is not held in memory) with
> `Content-Type: application/pdf` and
> `Content-Disposition: attachment; filename="receipt-12.pdf"`. **OpenPDF** —
> the LGPL fork of iText 4 — avoids iText 7's AGPL licence, which requires
> open-sourcing the calling application or buying a commercial licence.
> **PDFBox** is Apache-licensed and better at manipulating existing documents
> than composing new ones. **Puppeteer** renders HTML with a headless Chromium
> and produces the best-looking output, at the cost of shipping a ~300 MB
> browser inside the container.
>
> Note that the *invoice email* already needs a document server-side, and
> currently sends an HTML-formatted message rather than a PDF attachment. That
> is the natural first place a server-side generator would be introduced.

\newpage

# What is not implemented

Stated in one place, deliberately, so nothing in this document overstates the
system.

| Topic | Status | What exists instead |
|---|---|---|
| Circuit Breaker | **Not implemented** | Manual provider switch; graceful degradation on every failure path |
| Standby payment microservice | **Not implemented** | Two implementations of one interface, selected by configuration |
| Server-side PDF generation | **Not implemented** | Client-side jsPDF; HTML invoice emails |
| Microservice architecture | **Not applicable** | Modular monolith, two interchangeable backends |
| Refresh tokens | **Not implemented** | Single access token; re-login on expiry |
| Route-level code splitting | **Not implemented** | Component-level lazy loading for heavy libraries only |
| A shared `useApi` hook | **Not implemented** | A single `client.js` wrapper |
| CI pipeline | **Not implemented** | Local builds and `docker compose` |
| Automated tests | **Not implemented** | Manual verification and static analysis |

Every one of these is a reasonable question in a viva. Knowing precisely which
are missing, and being able to say what you would do about each, reads far
better than being caught claiming one that is not there.

\newpage

# Implementation Summary and Interview Narrative

*The following is written in the first person, to be read aloud or adapted.*

## The two-minute version

"I built e-Tour, a full-stack tour booking platform. It's a React single-page
application on the front, a Spring Boot 4 backend, and MySQL, with the whole
thing running as four Docker containers behind nginx.

The part I'd point to first is that I built the **same backend twice** — once in
Spring Boot and once in ASP.NET Core — with identical REST contracts. nginx has
a single `upstream` block, so switching the entire system from Java to .NET is
one commented line and a container restart. The React code contains no backend
URLs at all. That forced me to be disciplined about what actually belongs in an
API contract, because anything I got wrong in one showed up immediately as a
difference in the other.

On security, authentication is stateless JWT with HS512, passwords are BCrypt,
and there's Google OAuth on top. The decision I'd defend is that I deliberately
keep **roles out of the token** and load them from the database on each request.
It costs a read per request, but it means an administrator I demote loses access
immediately rather than whenever their token happens to expire.

For payments I integrated Razorpay. The important part isn't the integration,
it's the verification — the browser hands back an order id, a payment id and a
signature, and none of that is trusted. The server recomputes an HMAC of
`orderId|paymentId` with the key secret, compares it in constant time, and then
independently asks Razorpay what amount was actually captured and checks it
against the total the server recomputed from the fare bands. Without that second
check, someone could create a one-rupee order and use it to book a five-thousand
rupee tour.

I used Aspect-Oriented Programming for the cross-cutting concerns — request
correlation, an audit trail across seven business actions, and slow-call
detection — so none of that is smeared through the service layer.

And emails are asynchronous on a dedicated thread pool, because an SMTP timeout
should never fail a booking that has already been paid for and committed."

## If they ask what you would improve

"Three things, and I'd order them by risk.

**First, a circuit breaker around the payment gateway.** Right now, if Razorpay
degrades, every checkout waits for the timeout. I'd add Resilience4j with a
sliding-window breaker. The subtle part is what the fallback does — for most
services you return cached data, but a payment fallback must never fabricate a
success. The only safe fallback is to park the booking as `PAYMENT_PENDING` and
reconcile it, because the alternative is a confirmed booking nobody paid for.

**Second, tests.** I verified everything by hand and with static analysis. The
booking pricing logic — five fare bands, age computed at departure, occupancy
limits — is exactly the kind of pure business rule that should have unit tests,
and it's where a regression would be most expensive.

**Third, the token storage.** I keep the JWT in `localStorage`, which is exposed
to XSS. For anything handling real money I'd move to an `httpOnly`, `Secure`,
`SameSite=Strict` cookie with a short access token and a refresh token — and
then I'd have to re-enable CSRF protection, which is safe to disable today
precisely *because* the token isn't sent automatically."

## Three debugging stories worth telling

Interviewers remember specifics. Each of these is real.

**The audit log that logged nothing.** I'd written an AOP pointcut naming
`ReviewService.createReview`. The method is actually called `submitReview`.
Pointcuts are strings — the compiler doesn't check them — so it compiled,
started, and silently audited nothing. What I took from it is that a silent
subsystem needs to prove it's alive, so I added a `@PostConstruct` hook that logs
which action groups are active and the resolved absolute path of the log file on
every boot.

**OAuth and email dying in Docker with no error at all.** Both worked locally
and neither worked in a container, with nothing in the logs. There turned out to
be several independent causes, but the one worth repeating is that I'd got a
configuration key subtly wrong. A *wrong* key isn't an error to a framework —
it's an **absent** key, and absent keys take their defaults. Nothing was
misconfigured from the framework's point of view, so nothing was logged. That
changed how I debug configuration: I now check what the application actually
resolved, not what I think I set.

**The delete that failed differently in each backend.** Deleting a tour threw a
foreign-key error. In Java, JPA `cascade = ALL` handled six of the seven child
tables — reviews deliberately weren't cascaded — so I only needed to remove
those explicitly. But EF Core **doesn't cascade** with nullable foreign keys; it
sets them null instead. The same logical fix needed completely different code on
each side. It's a good illustration of why two implementations of one contract
teaches you more than one — the differences are where the real understanding is.

## Honest framing of the whole project

"It's a modular monolith, not microservices, and I'd say that plainly. Two
backends, four containers, clean layer separation and a strategy pattern at the
payment boundary — but one deployable per backend. For a system this size that's
the right call; splitting it into services would have added network calls,
distributed transactions and deployment complexity to solve a scaling problem I
don't have.

What I'd want to be judged on isn't the feature count. It's that the security
decisions were deliberate and I can explain the trade-off behind each one, and
that when things broke I diagnosed them from the *shape* of the error rather
than by changing code until it worked."
