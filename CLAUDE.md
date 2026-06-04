# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repo context

This is `AQ-SIMPLE-BACK`, the Spring Boot REST API of the AQ (Auto-Questionnaire) project. The companion Angular frontend lives in the sibling directory `AQ-SIMPLE-FRONT`. A workspace-wide `CLAUDE.md` exists in the parent directory (`F:\DEV\AQ\CLAUDE.md`) — when its content conflicts with what is here (notably the REST endpoint list, which is outdated in the parent), this file is authoritative for the backend.

Stack: Spring Boot 3.1.5, Java 17, Maven (wrapper), PostgreSQL 15, Lombok, Jackson (with `@JsonView`), jsoup, logstash-logback-encoder.

## Commands

```bash
# Bring up Postgres + pgAdmin (required before running the app)
docker-compose up -d

# Run the app
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=DesplegableCompartidoTest

# Run a single test method
./mvnw test -Dtest=DesplegableCompartidoTest#nombreDelMetodo

# Build the jar
./mvnw package
```

Postgres runs at `localhost:5432` (`postgres` / `postgres`, db `postgres`). pgAdmin at `localhost:5050` (`lorenzomar@mail.com` / `1234`). Schema is managed by Hibernate `ddl-auto=update` — there are no migrations; entity changes mutate the live DB on next boot.

## Architecture

### The `AResponder` hierarchy is the whole domain model

Everything that can be "answered" — questionnaires, topics, subtopics, and every concrete question type — extends the abstract `AResponder` (`model/AResponder/AResponder.java`). It uses JPA `InheritanceType.JOINED`, so each subclass has its own table joined on `id`. The `TipoAResponder` enum on `AResponder.tipo` is the runtime discriminator.

Two branches under `AResponder`:

- **`Temario`** — container. Holds `List<AResponder> listaAResponder` (its children). Used for `CUESTIONARIO` (top-level), `TEMA`, and `SUBTEMA`. A `SUBTEMA` cannot contain another `Temario` (enforced in `Temario.agregarALaLista`).
- **`Pregunta`** (abstract) — base for every concrete question subclass: `PreguntaSimple`, `VerdaderoOFalso`, `SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`.

Three collaborators wire the hierarchy together:

- **`FabricaDePreguntas`** (`model/AResponder/FabricaDePreguntas.java`) — factory that maps `TipoAResponder` → concrete `Pregunta` subclass for inbound DTOs. Uses `BeanUtils.copyProperties` to hydrate.
- **`AsignadorDeTipoALasPreguntas`** (enum singleton) — inverse of the factory: maps `Class<? extends Pregunta>` → `TipoAResponder`. Called from `Pregunta.asignacionDeTipo()` and from the Strategy below.
- **`TipoDeTemario`** (Strategy) — `TipoCuestionario` vs `TipoTema`. Selected in `Temario.@PostLoad init()` based on `tipo`. Determines what `TipoAResponder` is assigned to a child when it is added via `agregarALaLista`: a child `Temario` becomes `TEMA` under a `CUESTIONARIO` or `SUBTEMA` under a `TEMA`; a `Pregunta` is delegated to `AsignadorDeTipoALasPreguntas`.

When adding a new question type you must touch all three: subclass `Pregunta`, register in `FabricaDePreguntas` map, register in `AsignadorDeTipoALasPreguntas` map, add a value to `TipoAResponder`, and add a matching repository under `Repository/PreguntaRepository/`.

### "Crítico" (review-flagged) tracking

`Pregunta.intentosParaQueDejeDeSerCriticoDisponible` is an `Integer` counter on every question.
- Wrong answer → set to `3`.
- Correct answer → decrement if > 0.
- `contieneCritico()` returns true iff `> 0`.

The state is mutated inside `verificarSiLaRespuestaEsCorrectaYAsignarCriticos`, called from `PreguntaService.verifyResponse` (which then saves). Answer correctness itself is delegated to each subclass's `laRespuestaEsCorrecta(RespuestaDePreguntaDTO)`.

### Jackson `@JsonView` controls serialization shape per endpoint

`model/View.java` defines two groups: `JustToAnswer` and `Full extends JustToAnswer`. Fields annotated `@JsonView(JustToAnswer.class)` serialize in both views; `@JsonView(Full.class)` only in `Full`. Controllers pick the view per endpoint — e.g., `POST /questions/fetch` uses `JustToAnswer` (no theory leaked to a user mid-quiz), `POST /questions/fetch-full` uses `Full` (theory included, for edit/review screens). When adding fields, choose the view group deliberately — omitting the annotation means the field never serializes.

### Layering and packages

- `Controller/` → `Service/` → `Repository/` (Spring Data JPA).
- `dto/newDto/` — request/response DTOs (the `newDto` name is historical, this is the active package).
- `dto/conversor/` — `TemarioDTOConversor`, `AResponseItemDTOConversor` for entity↔DTO mapping.
- `projections/` — JPA interface projections (`QuestionnaireItem`, `IssueOrQuestionnaireProjection`) for optimized reads that avoid hydrating the full entity graph.
- `Repository/PreguntaRepository/` — one repo per concrete question type, all extending `BasePreguntaRepositorio<T>`. `PreguntaService` keeps a `Map<TipoAResponder, BasePreguntaRepositorio<?>>` (built in `@PostConstruct init()`) to dispatch by type.
- `config/setup/Setup.java` — `ApplicationRunner` that can seed demo data (`Electricidad y magnetismo` etc.). The body of `run()` is currently commented out; uncomment `datos(); guardarCuestionario();` to reseed.
- `exception/BussinesException.java` — domain exception (note the spelling — keep it as-is for consistency, not "Business").

### Transactions and lazy loading

`spring.jpa.open-in-view=false`. Lazy associations (e.g., `Temario.listaAResponder`, `Pregunta.listaDeTeoriaDeLaPregunta`) **will not** load outside a transaction. Endpoints that touch lazy state must be `@Transactional` (see `getTopicContent`, `obtenerIdsPreguntas`, `getQuestionFull`). Adding a new endpoint that traverses entity graphs without `@Transactional` will throw `LazyInitializationException` at serialization time.

## REST API

All controllers are annotated `@CrossOrigin(origins = {"*"})` — there is no auth layer. The endpoints below reflect the actual code in `Controller/`; the parent `CLAUDE.md` lists older paths (`/allCuestionario`, `/createIssue`, `/delete/{id}`, etc.) that no longer exist.

### `TemarioController` (questionnaires / topics)

| Method | Path | Notes |
|---|---|---|
| GET | `/questionnaires` | All top-level (`CUESTIONARIO`) temarios |
| POST | `/questionnaires` | Create a top-level questionnaire |
| GET | `/issues/{id}/items` | Children of a temario, projection-based (`@Transactional`) |
| POST | `/issues` | Create child temario; body sets `fatherid` |
| PUT | `/issues` | Update temario |
| DELETE | `/issues/{id}` | Delete temario |
| GET | `/issues/{id}/question-ids` | All question IDs under the temario subtree |
| POST | `/issues/inverse` | Build a new temario from inverted `PREGUNTA_SIMPLE` children |

### `PreguntaController` (questions)

| Method | Path | View | Notes |
|---|---|---|---|
| POST | `/questions/fetch` | `JustToAnswer` | Question without theory |
| POST | `/questions/fetch-full` | `Full` | Question with theory (`@Transactional` — loads lazy theory set) |
| POST | `/questions` | — | Create; dispatched through `FabricaDePreguntas` |
| PUT | `/questions` | `JustToAnswer` | Update |
| DELETE | `/questions/{id}` | — | Delete |
| POST | `/questions/verify` | — | Verifies user answer; mutates `intentosParaQueDejeDeSerCritico…` |
| POST | `/questions/inverse` | — | Inverse-question creation (only `PREGUNTA_SIMPLE` currently); uses `Jsoup.parse(...).text()` to strip HTML |

### `ResponderController` (answer flow)

| Method | Path | Notes |
|---|---|---|
| POST | `/questions/random-ids` | Returns a shuffled list of question IDs |
| GET | `/questions/{id}/critical-ids` | Returns IDs of "crítico" questions under a node |

## Conventions worth knowing

- Domain code is in **Spanish** (entities, services, fields: `Temario`, `Pregunta`, `intentosParaQueDejeDeSerCriticoDisponible`). Controllers expose **English** REST paths. New code should follow the same split — do not anglicize existing identifiers.
- Lombok `@Getter`/`@Setter` is used throughout; do not hand-write accessors.
- Logger pattern: `logger.info("[METHOD /path] field={}", value)` at controller entry (see existing controllers for examples).
- `BussinesException` (sic) is the standard not-found / domain-error throw; controllers do not have a `@ControllerAdvice`, so it surfaces as a 500 — keep this in mind when changing error handling.
- `Setup.datos()` builds an in-memory example tree using the **builder-on-the-entity** style (`new Temario(...)`, `agregarALaLista(...)`). Use it as a reference when constructing test fixtures.
