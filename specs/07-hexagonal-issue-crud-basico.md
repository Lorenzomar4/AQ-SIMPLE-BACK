# Spec 07 — Migración hexagonal: CRUD básico de Temario/Issue

**Estado:** Implemented
**Dependencias:** Spec 01 (hexagonal-content-piloto) — reutiliza `content.domain.AResponder`/`Temario`, `content.infrastructure.persistence.entity.{AResponderEntity, TemarioEntity}`, `TemarioMapper`, `TemarioJpaAdapter`, `TemarioJpaRepository`, `TemarioRepositoryPort` (extiende su contrato con `save`/`deleteById`); reutiliza el `Repository/TemarioRepository` (JPA viejo) solo para el borrado, preservando su cascada de eliminación sobre `listaAResponder`.
**Fecha:** 2026-08-01
**Objetivo:** Migrar el CRUD básico de `Temario`/`Issue` (crear cuestionario top-level, crear issue hijo, editar issue, borrar issue) a arquitectura hexagonal, dejando `GET /issues/{id}/question-ids` y `POST /issues/inverse` para un spec posterior por su complejidad distinta (recursión de árbol e inversión de preguntas).

---

## Alcance

### Incluido

- **Domain puro:**
  - `content.domain.AResponder` — se agrega `ultimaActualizacion: LocalDateTime`. Heredado automáticamente por `Temario` (y por extensión por todos los tipos de pregunta ya migrados, que simplemente no lo usan todavía).
  - `content.domain.Temario` — sin cambios estructurales (sigue anémico, sin `listaAResponder`); las operaciones de este spec no necesitan navegar hijos en memoria.
- **Infraestructura de persistencia:**
  - `AResponderEntity` — se agrega columna `ultimaActualizacion` (`@Temporal(TemporalType.TIMESTAMP)`), mapeada a la misma columna que ya usa el modelo viejo.
  - `TemarioMapper` — se agrega `toEntity(Temario)` (hasta ahora solo tenía `toDomain`), y `toDomain` mapea también `ultimaActualizacion`.
  - `TemarioRepositoryPort` — se extiende con `Temario save(Temario)` y `void deleteById(Long id)`.
  - `TemarioJpaAdapter` — implementa `save` delegando en `TemarioJpaRepository` (nuevo) + `TemarioMapper`; implementa `deleteById` delegando directamente en `Repository.TemarioRepository` (el JPA **viejo**, inyectado en el adapter), para preservar la cascada real de borrado sobre `listaAResponder` (`@OneToMany(cascade = CascadeType.ALL)`) sin reimplementarla.
- **Application — puertos dedicados a las 4 operaciones de escritura de Temario/Issue:**
  - Puertos in: `CrearCuestionarioUseCase` (POST /questionnaires), `CrearIssueUseCase` (POST /issues), `EditarIssueUseCase` (PUT /issues), `EliminarIssueUseCase` (DELETE /issues/{id}).
  - Servicios:
    - `CrearCuestionarioService` — crea un `Temario` con `tipo = CUESTIONARIO`, `fechaDeCreacion`/`ultimaActualizacion = now()`, sin `idDuenio` (no tiene padre), y lo persiste.
    - `CrearIssueService` — busca el padre por `fatherid` vía `TemarioRepositoryPort.findById` (`BussinesException` si no existe); si `padre.tipo == SUBTEMA` lanza `BussinesException("No se puede agregar un subtema a otro subtema")` (mismo mensaje que el modelo viejo); si no, determina `tipo` del hijo de forma directa (`CUESTIONARIO → TEMA`, `TEMA → SUBTEMA`) sin reutilizar las clases `TipoDeTemario`/`TipoCuestionario`/`TipoTema` viejas (operan sobre el modelo JPA viejo, no sobre `content.domain`); construye el hijo (`idDuenio = padre.id`, `fechaDeCreacion`/`ultimaActualizacion = now()`) y lo persiste como fila independiente (sin pasar por la colección `listaAResponder` del padre), mismo criterio de "asignación directa" ya usado en specs 02-06 para preguntas. Devuelve el `AResponderItemListDTO` **del hijo recién creado** (`numberOfQuestions = null`, `isCritic = false`, igual que produciría hoy un `Temario` recién creado y vacío).
    - `EditarIssueService` — busca por id vía el puerto (`BussinesException` si no existe, mismo mensaje que hoy: `"Error , no existe este cuestionario"`), actualiza `titulo` y `ultimaActualizacion = now()`, persiste.
    - `EliminarIssueService` — busca por id vía el puerto y lanza `BussinesException` si no existe; si existe, llama a `deleteById` del puerto (que cascadea vía el repositorio viejo).
- **Dispatch en `TemarioController`:** `POST /questionnaires`, `PUT /issues`, `POST /issues` y `DELETE /issues/{id}` delegan directamente a los nuevos use cases (no hay bifurcación por tipo, a diferencia de `PreguntaController`, porque estas rutas solo tratan con `Temario`). `GET /questionnaires` sigue como ya está (migrado en spec 01); `GET /issues/{id}/items`, `GET /issues/{id}/question-ids` y `POST /issues/inverse` no se tocan.
- **Tests:** tests unitarios puros de la nueva lógica de asignación de tipo en `CrearIssueService` (sin Spring) + al menos un test de integración de paridad (`IssueParidadTest` o similar) comparando las 4 operaciones contra `TemarioService` viejo, incluyendo el caso de error "no se puede agregar subtema a subtema" y la verificación de que el borrado cascadea a los hijos.

### Explícitamente NO incluido

- `GET /issues/{id}/question-ids` y `POST /issues/inverse` — quedan en el código viejo (`TemarioService.obtenerTodosLosIdsDePreguntas`, `crearTemarioPreguntasInversa`), se evalúan en un spec posterior por su complejidad distinta (recursión sobre el árbol, dependencia de `PreguntaSimple.inversar()`).
- `GET /questionnaires` (ya migrado en spec 01) y `GET /issues/{id}/items` — no se tocan.
- Eliminar `TemarioService` ni sus métodos migrados (`saveTemarioCuestionario`, `actualizarCuestionario`, `crearNuevoTemarioHijo`, `eliminarCuestionario`) — quedan como código muerto (necesarios además como baseline de los tests de paridad de este mismo spec); su remoción se difiere a una limpieza final.
- Reutilizar las clases viejas `TipoDeTemario`/`TipoCuestionario`/`TipoTema` — se descarta explícitamente (ver Alcance incluido); se reimplementa la lógica de asignación de tipo de forma directa en `CrearIssueService`.
- Añadir `listaAResponder` (u otra forma de navegar hijos) a `content.domain.Temario` — no es necesario para las 4 operaciones de este spec.
- Cualquier cambio de esquema de base de datos — se reutilizan `temario`/`aresponder` sin modificarlas; `ultimaActualizacion` ya existe como columna real (usada hoy por el modelo viejo).
- Mover `TemarioController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Verificar `ON DELETE CASCADE` a nivel de Postgres — no es necesario porque se delega el borrado al repositorio JPA viejo, que ya cascadea vía el grafo de Hibernate.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder`, `TemarioBasicDTO(id, name, creationDate, fatherid)`, `AResponderItemListDTO(id, name, type, numberOfQuestions, isCritic)` — mismos contratos de wire, sin tocar.
- `content.domain.Pregunta` y sus 6 subclases, `PreguntaEntity`, `BaseContentRepositorio<T>` — no se tocan (este spec solo extiende `AResponder`/`Temario`, que están más arriba en la jerarquía y no afectan a las preguntas).
- `Repository.TemarioRepository` (JPA viejo) — se reutiliza tal cual para el borrado; no se le agrega ni modifica ningún método.

### `content/domain/` (modificado)

- **`AResponder`** — se agrega `ultimaActualizacion: LocalDateTime`.
- **`Temario`** — sin cambios (hereda el campo nuevo de `AResponder`).

### `content/infrastructure/persistence/` (modificado)

- **`AResponderEntity`** — se agrega `@Temporal(TemporalType.TIMESTAMP) private LocalDateTime ultimaActualizacion;`, mapeada a la columna real `ultima_actualizacion` (`timestamp(6)`, confirmada contra el DDL provisto).
- **`TemarioMapper`** — se agrega `toEntity(Temario)`; `toDomain` mapea también `ultimaActualizacion`.
- **`TemarioRepositoryPort`** — se agregan `Temario save(Temario)` y `void deleteById(Long id)`.
- **`TemarioJpaAdapter`** — `save` vía `TemarioJpaRepository` (nuevo) + `TemarioMapper`; `deleteById` vía `Repository.TemarioRepository` (viejo), inyectado además del `TemarioJpaRepository` nuevo.

### `content/application/` (nuevo)

- **Puertos in:** `CrearCuestionarioUseCase`, `CrearIssueUseCase`, `EditarIssueUseCase`, `EliminarIssueUseCase`.
- **Servicios:** `CrearCuestionarioService`, `CrearIssueService`, `EditarIssueService`, `EliminarIssueService`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. Confirmado contra el DDL real provisto por el usuario:
- `aresponder` (`id`, `descripcion` oid, `fecha_de_creacion`, `id_del_duenio` FK a `temario` **sin** `ON DELETE CASCADE`, `tipo` con `check` constraint que ya incluye `TEMA`/`SUBTEMA`/`CUESTIONARIO`, `titulo` varchar(10500), `ultima_actualizacion` timestamp(6)).
- `temario` (única columna propia: `id`, FK a `aresponder`).
- El FK `fkfhr1in1fs32khyjpolrnvke7a` (`aresponder.id_del_duenio → temario.id`) es la pieza crítica del riesgo de borrado: sin cascada de BD, un `delete from temario` directo (o cualquier borrado que no pase por el grafo JPA cargado) fallaría por violación de integridad referencial en cuanto el `Temario` tuviera al menos un hijo.

---

## Plan de implementación

1. **Confirmación de columna** `ultima_actualizacion` contra el DDL real (ya hecho, `timestamp(6)` en `aresponder`).
2. **Domain puro.** Agregar `ultimaActualizacion: LocalDateTime` a `content/domain/AResponder.java`.
3. **Entity JPA.** Agregar `ultimaActualizacion` (`@Temporal(TemporalType.TIMESTAMP)`) a `AResponderEntity`. Levantar la app y confirmar que arranca sin error de mapeo (`ddl-auto=validate`).
4. **Mapper.** Agregar `toEntity(Temario)` a `TemarioMapper`; actualizar `toDomain` para mapear también `ultimaActualizacion`.
5. **Puerto y adapter.** Extender `TemarioRepositoryPort` con `save(Temario)` y `deleteById(Long)`. Implementar en `TemarioJpaAdapter`: `save` vía `TemarioJpaRepository` (nuevo); `deleteById` vía `Repository.TemarioRepository` (viejo, inyectado adicionalmente en el adapter) para preservar la cascada real sobre `listaAResponder`.
6. **Puertos in y servicios de escritura.**
   - `CrearCuestionarioUseCase`/`Service` — crea `Temario` con `tipo = CUESTIONARIO`.
   - `CrearIssueUseCase`/`Service` — busca el padre, valida que no sea `SUBTEMA`, determina el tipo del hijo (`CUESTIONARIO→TEMA`, `TEMA→SUBTEMA`), persiste el hijo como fila independiente, devuelve su `AResponderItemListDTO`.
   - `EditarIssueUseCase`/`Service` — busca por id, actualiza `titulo` y `ultimaActualizacion`, persiste.
   - `EliminarIssueUseCase`/`Service` — verifica existencia (`BussinesException` si no existe) y borra vía el puerto.
7. **Test de paridad.** Crear `IssueParidadTest` (integración) comparando contra `TemarioService` viejo:
   - Creación de cuestionario top-level.
   - Creación de issue hijo bajo un `CUESTIONARIO` (verifica que el hijo queda `TEMA`) y bajo un `TEMA` (verifica que queda `SUBTEMA`).
   - Caso de error: intentar agregar un hijo a un `SUBTEMA` (debe lanzar `BussinesException` en ambos caminos).
   - Edición de `titulo` (y que `ultimaActualizacion` se actualice).
   - Borrado de un `Temario` sin hijos y de un `Temario` **con hijos** (verificando que los hijos también se borran de la base, sin violar el FK `aresponder.id_del_duenio → temario`).
8. **Cablear `TemarioController`.** Reemplazar las llamadas a `temarioService` por los nuevos casos de uso en `POST /questionnaires`, `PUT /issues`, `POST /issues` y `DELETE /issues/{id}`. `GET /questionnaires`, `GET /issues/{id}/items`, `GET /issues/{id}/question-ids` y `POST /issues/inverse` no se tocan.
9. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: crear un cuestionario, crear un tema y un subtema dentro de él, editar el título de un issue, borrar un issue sin hijos y uno con hijos (incluyendo preguntas dentro), y confirmar que la lectura (`GET /questionnaires`, `GET /issues/{id}/items`) y el CRUD de los 6 tipos de pregunta ya migrados siguen funcionando sin cambios.

---

## Criterios de aceptación

- [x] `content.domain.AResponder` compila con el nuevo campo `ultimaActualizacion: LocalDateTime`, sin ninguna anotación de Spring/JPA/Jackson.
- [x] `AResponderEntity` mapea `ultimaActualizacion` a la columna real `ultima_actualizacion` (`timestamp(6)`); al levantar la app con `ddl-auto=validate` no se crean tablas ni columnas nuevas en Postgres.
- [x] `TemarioMapper` tiene `toEntity`/`toDomain` simétricos, incluyendo `ultimaActualizacion`.
- [x] `POST /questionnaires` crea un `Temario` con `tipo = CUESTIONARIO`, produciendo un resultado equivalente al flujo viejo.
- [x] `POST /issues` crea un issue hijo: bajo un `CUESTIONARIO` el hijo queda `TEMA`; bajo un `TEMA` el hijo queda `SUBTEMA`; bajo un `SUBTEMA` lanza `BussinesException("No se puede agregar un subtema a otro subtema")`. La respuesta es el `AResponderItemListDTO` del hijo creado.
- [x] `PUT /issues` actualiza `titulo` y `ultimaActualizacion` de un issue existente; lanza `BussinesException` si el id no existe.
- [x] `DELETE /issues/{id}` verifica existencia (`BussinesException` si no existe) y borra el issue; si el issue tiene hijos (temas, subtemas y/o preguntas de cualquiera de los 6 tipos), estos también se borran, sin violar el FK `aresponder.id_del_duenio → temario`.
- [x] `GET /questionnaires`, `GET /issues/{id}/items`, `GET /issues/{id}/question-ids` y `POST /issues/inverse` siguen funcionando sin cambios.
- [x] El CRUD de los 6 tipos de pregunta ya migrados (specs 02-06) sigue funcionando sin cambios.
- [x] Existe `IssueParidadTest`, con tests de paridad para: creación de cuestionario, creación de issue hijo (bajo cuestionario y bajo tema), el caso de error de subtema anidado, edición, borrado simple y borrado en cascada con hijos.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: crear cuestionario, crear tema/subtema, editar título, borrar un issue sin hijos y uno con hijos (incluyendo preguntas) funcionan sin errores visibles. **(verificado por el usuario)**
- [x] `TemarioService` (viejo) no fue eliminado; sus métodos migrados quedan sin uso desde el controller pero intactos como baseline de los tests de paridad.

---

## Decisiones tomadas y descartadas

- **El borrado se delega al `Repository.TemarioRepository` (JPA viejo), no a un `deleteById` propio del `TemarioJpaRepository` nuevo.**
  Confirmado con el usuario. Justificación: el DDL real confirma que `aresponder.id_del_duenio → temario.id` **no tiene `ON DELETE CASCADE`**; el borrado en cascada de hoy depende exclusivamente del grafo JPA del modelo viejo (`@OneToMany(cascade = CascadeType.ALL)` sobre `listaAResponder`). Reimplementar un borrado recursivo manual o confiar en una cascada de BD inexistente habría sido más riesgoso y redundante frente a reutilizar el mecanismo que ya funciona.

- **`POST /issues` devuelve el `AResponderItemListDTO` del hijo recién creado, no el del padre** (a diferencia del código viejo, que por lo que parece un descuido devuelve el DTO del padre).
  Confirmado con el usuario. Justificación: es la respuesta semánticamente correcta para un endpoint de creación, y no hay riesgo de romper el frontend porque `IssueServiceService.saveSimpleIssue` ignora completamente el body de la respuesta.

- **Se agrega `ultimaActualizacion` a `content.domain.AResponder`/`AResponderEntity`**, aunque ningún DTO de respuesta lo expone todavía.
  Confirmado con el usuario. Justificación: evita perder ese dato silenciosamente al persistir una edición por el camino nuevo; la columna ya existe en la BD real y el modelo viejo la mantiene actualizada en cada edición.

- **`EliminarIssueService` verifica existencia primero y lanza `BussinesException`**, en vez de preservar el comportamiento actual (`deleteById` sin chequeo, que deja que Spring Data lance `EmptyResultDataAccessException` si no existe).
  Confirmado con el usuario. Justificación: consistencia con los demás casos de uso de este mismo spec (`CrearIssueService`, `EditarIssueService`), que sí verifican existencia y usan `BussinesException` como excepción de dominio estándar del proyecto.

- **No se reutilizan las clases viejas `TipoDeTemario`/`TipoCuestionario`/`TipoTema`**; la asignación de tipo del hijo se reimplementa de forma directa (if/else) en `CrearIssueService`.
  Descartado: adaptar esas clases para que operen sobre `content.domain`. Justificación: son un Strategy diseñado sobre el modelo JPA viejo (reciben `AResponder` viejo, delegan en `AsignadorDeTipoALasPreguntas` para preguntas); como este spec solo necesita la rama "agregar un `Temario` a otro `Temario`" (nunca una pregunta), la lógica útil se reduce a dos condicionales — replicarla directo evita acoplar el domain nuevo a las clases viejas sin necesidad real.

- **El hijo se persiste como fila independiente (idDuenio + tipo asignados directamente), no a través de `Temario.agregarALaLista` ni de una colección `listaAResponder` cargada en memoria.**
  Mismo criterio ya establecido en specs 02-06 para la creación de preguntas: evita tener que modelar la relación bidireccional en `content/domain`/`content/infrastructure` solo para un insert que no la necesita.

- **`TemarioService` (viejo) no se elimina en este spec.**
  Confirmado con el usuario. Justificación: sus métodos migrados (`saveTemarioCuestionario`, `actualizarCuestionario`, `crearNuevoTemarioHijo`, `eliminarCuestionario`) quedan como baseline necesario para `IssueParidadTest`; su remoción se difiere a una limpieza final, mismo criterio que specs anteriores de no tocar código fuera del camino que se está migrando activamente.

- **`GET /issues/{id}/question-ids` y `POST /issues/inverse` quedan fuera de este spec.**
  Confirmado con el usuario al inicio de la definición. Justificación: complejidad genuinamente distinta (recursión sobre el árbol completo, dependencia de `PreguntaSimple.inversar()`) — mismo criterio de "un problema a la vez" usado en specs 02-06 para no mezclar decisiones de dominios distintos en un solo spec.

---

## Riesgos identificados

- **Borrado en cascada depende de que el grafo JPA esté completamente cargado antes del `remove`.** `Repository.TemarioRepository.deleteById(id)` (Spring Data) internamente hace `getReferenceById` + `delete`; si Hibernate no inicializa la colección `listaAResponder` (`FetchType.LAZY`) antes de aplicar la cascada, podría no borrar todos los hijos y luego fallar por el FK `aresponder.id_del_duenio → temario` (sin `ON DELETE CASCADE` en BD, confirmado contra el DDL real). Esto ya es un comportamiento existente del código viejo (no introducido por este spec), pero al empezar a depender de él desde un caso de uso nuevo conviene verificarlo explícitamente.
  *Mitigación:* el test de paridad del paso 7 incluye un borrado de un `Temario` **con hijos** (temas/subtemas y al menos una pregunta), verificando que no queden filas huérfanas ni se lance una excepción de integridad referencial.

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas `aresponder`/`temario`**, mismo riesgo ya identificado en specs 01-06, ahora extendido a operaciones de escritura sobre el nodo padre del árbol en vez de solo hojas (preguntas). Un `save`/`delete` por el camino nuevo mientras el camino viejo tiene una instancia en su propio `PersistenceContext` podría generar inconsistencias si ambos caminos operan sobre el mismo `Temario` en la misma transacción.
  *Mitigación:* mismo criterio que specs anteriores — el test de paridad corre contra estado limpio, ejecutando cada camino (viejo/nuevo) en su propia operación, no simultáneamente sobre la misma fila.

- **`CrearIssueService` reimplementa la lógica de `TipoDeTemario` de forma manual**, en lugar de reutilizar las clases existentes. Un desajuste sutil (por ejemplo, invertir la asignación `CUESTIONARIO→TEMA`/`TEMA→SUBTEMA`) no sería detectado por el compilador y podría pasar desapercibido en una revisión rápida.
  *Mitigación:* el test de paridad del paso 7 verifica explícitamente el tipo resultante del hijo en ambos casos (bajo `CUESTIONARIO` y bajo `TEMA`), comparando contra el resultado del camino viejo.

- **`ultimaActualizacion` se agrega recién en este spec a `content.domain`/`content.infrastructure`, pero la columna real ya tiene datos históricos** escritos por el modelo viejo. Si algún `Temario` existente fue creado antes de este spec y se edita por primera vez con el camino nuevo, el valor previo se sobrescribe igual que lo haría el camino viejo — no hay incompatibilidad, pero vale la pena confirmarlo en la verificación manual.
  *Mitigación:* cubierto por el paso 9 (verificación manual contra `AQ-SIMPLE-FRONT`, editando un issue preexistente).
