# Spec 09 — Migración hexagonal: cableado de DELETE /questions/{id}

**Estado:** Implementado
**Dependencias:** Specs 01-06 (hexagonal-content-piloto, verdadero-o-falso, seleccion-unica, opcion-multiple, desplegable-compartido, desplegable-independiente) — reutiliza los 6 `EliminarXxxUseCase`/`Service` ya implementados en esos specs pero nunca cableados al controller; Spec 07/08 (hexagonal-issue-crud-basico / question-ids-inverse) — reutiliza el patrón de inyectar un repositorio JPA viejo dentro de un adapter nuevo.
**Fecha:** 2026-08-01
**Objetivo:** Cablear `DELETE /questions/{id}` a arquitectura hexagonal resolviendo primero el `TipoAResponder` del id recibido (vía un nuevo puerto que reutiliza `AResponderRepository` viejo) y despachando luego al `EliminarXxxUseCase` correspondiente de los 6 ya existentes.

---

## Alcance

### Incluido

- **Application — puerto out nuevo para resolver tipo:**
  - `AResponderTipoLookupPort` (`content.application.port.out`) — `Optional<TipoAResponder> findTipoById(Long id)`. Puramente interno, resuelve el tipo de cualquier `AResponder` (pregunta o issue) a partir de su id, sin exponerse en ningún DTO de wire.
  - `AResponderTipoLookupJpaAdapter` (`content.infrastructure.persistence.adapter`) — implementa el puerto inyectando `AResponderRepository` (viejo, `com.lorenzomar3.AQ.Repository`) y delegando en `findById(id).map(AResponder::getTipo)`. Mismo patrón de "reutilizar repositorio JPA viejo dentro de un adapter nuevo" usado en specs 07/08.
- **Application — orquestador de borrado por id:**
  - `EliminarPreguntaPorIdUseCase` (`content.application.port.in`) — `void eliminar(Long id)`.
  - `EliminarPreguntaPorIdService` — resuelve el tipo vía `AResponderTipoLookupPort.findTipoById` (`BussinesException` si no existe); despacha al `EliminarXxxUseCase` correspondiente de los 6 ya existentes (`EliminarPreguntaUseCase` para `PREGUNTA_SIMPLE`, `EliminarVerdaderoOFalsoUseCase`, `EliminarSeleccionUnicaUseCase`, `EliminarOpcionMultipleUseCase`, `EliminarDesplegableCompartidoUseCase`, `EliminarDesplegableIndependienteUseCase`) inyectándolos como beans de Spring y usando un `Map<TipoAResponder, EliminarXxxUseCase>` o if/else equivalente (mismo criterio de dispatch que ya usan `POST`/`PUT /questions` en `PreguntaController`); si el tipo resuelto es `CUESTIONARIO`/`TEMA`/`SUBTEMA` (id de un issue, no de una pregunta) lanza `BussinesException` — este endpoint nunca debería recibir un id de issue, pero se cubre el caso explícitamente en vez de dejarlo fallar de forma opaca.
- **Dispatch en `PreguntaController`:** `DELETE /questions/{id}` delega a `EliminarPreguntaPorIdUseCase` en vez de `preguntaService.delete(id)`.
- **Tests:** test de paridad comparando, para cada uno de los 6 tipos de pregunta, el borrado vía el camino nuevo contra `preguntaService.delete` (viejo) — mismo resultado final (fila eliminada, incluyendo cascada sobre sub-entidades propias del tipo, ej. opciones de `SeleccionUnica`/`OpcionMultiple`, sub-preguntas de `DesplegableIndependiente`); más un caso de id inexistente (`BussinesException`) y un caso de id de issue (`BussinesException`).

### Explícitamente NO incluido

- `POST /questions/fetch`, `POST /questions/fetch-full`, `POST /questions/verify`, `POST /questions/inverse` y todo `ResponderController` (`random-ids`, `critical-ids`) — quedan en código viejo, se evalúan en specs posteriores (opciones ya descartadas en la elección inicial de este spec).
- Modificar el comportamiento o la firma de los 6 `EliminarXxxUseCase`/`Service` ya existentes — se reutilizan tal cual, sin tocarlos.
- `PreguntaService.delete` (viejo) — no se elimina; queda como código muerto sin uso desde el controller, mismo criterio que `TemarioService` en spec 07.
- Cualquier cambio de esquema de base de datos.
- Mover `PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT` — el contrato de `DELETE /questions/{id}` no cambia (sigue recibiendo solo el id).
- Agregar verificación de existencia dentro de cada `EliminarXxxService` individual (ej. `EliminarPreguntaSimpleService`) — la verificación de existencia se hace una sola vez, en `EliminarPreguntaPorIdService`, antes de despachar.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- Los 6 `EliminarXxxUseCase`/`Service` (specs 01-06) — `EliminarPreguntaUseCase`, `EliminarVerdaderoOFalsoUseCase`, `EliminarSeleccionUnicaUseCase`, `EliminarOpcionMultipleUseCase`, `EliminarDesplegableCompartidoUseCase`, `EliminarDesplegableIndependienteUseCase` — sin ningún cambio de firma ni de comportamiento.
- `Repository.AResponderRepository` (JPA viejo, `JpaRepository<AResponder, Long>`) — se reutiliza tal cual su `findById(Long)` heredado; no se le agrega ni modifica ningún método.
- `com.lorenzomar3.AQ.model.TipoAResponder` — mismo enum, sin tocar.
- `com.lorenzomar3.AQ.exception.BussinesException` — mismo uso estándar ya establecido en specs anteriores.

### `content/application/port/out/` (nuevo)

- **`AResponderTipoLookupPort`** — interfaz nueva: `Optional<TipoAResponder> findTipoById(Long id)`. Puramente interno a la capa de aplicación, no se serializa ni se expone en ningún DTO de wire.

### `content/infrastructure/persistence/adapter/` (nuevo)

- **`AResponderTipoLookupJpaAdapter`** — implementa `AResponderTipoLookupPort`, inyectando `Repository.AResponderRepository` (viejo).

### `content/application/port/in/` y `content/application/service/` (nuevo)

- **`EliminarPreguntaPorIdUseCase`** — `void eliminar(Long id)`.
- **`EliminarPreguntaPorIdService`** — implementa el puerto; inyecta `AResponderTipoLookupPort` y los 6 `EliminarXxxUseCase` existentes.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutilizan `aresponder` y las tablas de los 6 tipos de pregunta sin modificarlas.

---

## Plan de implementación

1. **Puerto de lookup.** Crear `AResponderTipoLookupPort` (`content.application.port.out`) con `Optional<TipoAResponder> findTipoById(Long id)`.
2. **Adapter de lookup.** Implementar `AResponderTipoLookupJpaAdapter` (`content.infrastructure.persistence.adapter`), inyectando `Repository.AResponderRepository` (viejo) y delegando en `findById(id).map(AResponder::getTipo)`.
3. **Puerto in del orquestador.** Crear `EliminarPreguntaPorIdUseCase` (`content.application.port.in`) con `void eliminar(Long id)`.
4. **Servicio orquestador.** Implementar `EliminarPreguntaPorIdService`: inyecta `AResponderTipoLookupPort` y los 6 `EliminarXxxUseCase` existentes; resuelve el tipo (`BussinesException` si el id no existe, o si el tipo resuelto es `CUESTIONARIO`/`TEMA`/`SUBTEMA`); despacha vía `Map<TipoAResponder, EliminarXxxUseCase>` construido en `@PostConstruct` (mismo patrón que `PreguntaService.mapDeRepositorios`) al `EliminarXxxUseCase` correspondiente.
5. **Test de paridad.** Crear el test comparando, para cada uno de los 6 tipos de pregunta, el borrado vía `EliminarPreguntaPorIdService` contra `preguntaService.delete` (viejo): crear una pregunta de ese tipo, borrarla por el camino nuevo, verificar que ya no existe (incluyendo sub-entidades propias del tipo). Agregar el caso de id inexistente (`BussinesException`) y el caso de id de issue (`BussinesException`).
6. **Cablear `PreguntaController`.** Reemplazar `preguntaService.delete(id)` por `eliminarPreguntaPorIdUseCase.eliminar(id)` en `DELETE /questions/{id}`.
7. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: borrar una pregunta de cada uno de los 6 tipos, confirmar que un id inexistente da un error visible, y confirmar que el resto del CRUD (`POST`/`PUT /questions`, `GET /questions/fetch(-full)`, `GET /issues/{id}/items`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [x] Existe `AResponderTipoLookupPort` con `findTipoById(Long id): Optional<TipoAResponder>` en `content.application.port.out`.
- [x] `AResponderTipoLookupJpaAdapter` implementa el puerto reutilizando `AResponderRepository.findById` (viejo), sin duplicar lógica de consulta.
- [x] Existe `EliminarPreguntaPorIdUseCase`/`EliminarPreguntaPorIdService`, que resuelve el tipo antes de despachar y lanza `BussinesException` si el id no existe o corresponde a un issue (`CUESTIONARIO`/`TEMA`/`SUBTEMA`).
- [x] `EliminarPreguntaPorIdService` despacha correctamente a cada uno de los 6 `EliminarXxxUseCase` según el tipo resuelto, sin modificar su comportamiento.
- [x] `DELETE /questions/{id}` en `PreguntaController` delega en `EliminarPreguntaPorIdUseCase`, no en `preguntaService.delete`.
- [x] Borrar una pregunta de cualquiera de los 6 tipos por `DELETE /questions/{id}` produce el mismo resultado final que el camino viejo (fila y sub-entidades eliminadas).
- [x] `DELETE /questions/{id}` con un id inexistente lanza `BussinesException`.
- [x] `DELETE /questions/{id}` con un id de issue (`CUESTIONARIO`/`TEMA`/`SUBTEMA`) lanza `BussinesException`.
- [x] El resto de endpoints (`POST`/`PUT /questions`, `GET /questions/fetch(-full)`, `GET /issues/{id}/items`, y el CRUD de `Temario`/`Issue`) sigue funcionando sin cambios.
- [x] Existe un test de paridad cubriendo el borrado de los 6 tipos + caso not-found + caso id de issue.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: borrar preguntas de cada tipo y confirmar el error visible ante un id inexistente. **(verificado por el usuario)**
- [x] `PreguntaService.delete` (viejo) no fue eliminado; queda sin uso desde el controller.

---

## Decisiones tomadas y descartadas

- **Resolver el tipo vía un nuevo puerto (`AResponderTipoLookupPort`) que reutiliza `AResponderRepository` viejo**, en vez de que el controller consulte el repo viejo directamente o de cambiar el contrato del endpoint para incluir el tipo.
  Confirmado con el usuario. Descartado: (a) que `PreguntaController` inyecte `AResponderRepository` directamente — rompería la capa, el controller no debería hablar con JPA viejo; (b) modificar `DELETE /questions/{id}` para requerir tipo — tocaría `AQ-SIMPLE-FRONT`, algo que los specs anteriores evitaron explícitamente. Justificación: mantiene la capa de aplicación como único punto de resolución de tipo, sin tocar el contrato del endpoint.

- **`EliminarPreguntaPorIdService` verifica existencia y tipo antes de despachar, lanzando `BussinesException` explícito.**
  Confirmado con el usuario. A diferencia de los 6 `EliminarXxxService` individuales (que no verifican existencia — dejan que la falla ocurra más abajo si el id no existe), el orquestador sí lo hace, porque necesita el tipo para saber a quién despachar. Mismo criterio de "verificar antes de actuar" que `EliminarIssueService` (spec 07).

- **Un id de issue (`CUESTIONARIO`/`TEMA`/`SUBTEMA`) recibido en `DELETE /questions/{id}` lanza `BussinesException` explícito**, en vez de dejarlo fallar de forma opaca al no encontrar un `EliminarXxxUseCase` para ese tipo.
  Confirmado con el usuario. En la práctica nunca debería ocurrir (el frontend usa `DELETE /issues/{id}` para issues), pero cubre el caso de forma explícita en vez de silenciarlo.

- **El orquestador reutiliza los 6 `EliminarXxxUseCase` existentes** (inyectados como beans, dispatch vía `Map<TipoAResponder, EliminarXxxUseCase>`), en vez de saltárselos y llamar directo a cada `*RepositoryPort.deleteById`.
  Confirmado con el usuario. Descartado: dejar esas 6 interfaces como código muerto y reimplementar el borrado directo por puerto. Justificación: mismo patrón de dispatch por tipo que ya usan `POST`/`PUT /questions` en el controller; evita duplicar la lógica de borrado que esos 6 servicios ya encapsulan correctamente desde specs 01-06.

- **Nombre del orquestador: `EliminarPreguntaPorIdUseCase`/`EliminarPreguntaPorIdService`**, en vez de reutilizar `EliminarPreguntaUseCase` (ya tomado por `PreguntaSimple`, artefacto histórico de que `PreguntaSimple` fue el "piloto" de spec 01 y se llevó el nombre genérico).
  Confirmado con el usuario.

- **`PreguntaService.delete` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores (07, 08).

- **`POST /questions/fetch(-full)`, `POST /questions/verify`, `POST /questions/inverse` (individual) y todo `ResponderController` quedan fuera de este spec.**
  Elegidos explícitamente como alternativas descartadas al inicio de la definición de este spec, en favor de cerrar primero el cabo suelto de `DELETE /questions/{id}` (menor riesgo, sin dominio nuevo).

---

## Riesgos identificados

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas**, ahora también en el camino de lookup: `AResponderTipoLookupJpaAdapter` lee por `AResponderRepository` (viejo) mientras el `EliminarXxxService` correspondiente borra por su repositorio nuevo (`PreguntaSimpleJpaRepository`, etc.). Mismo riesgo ya identificado en specs 01-08, ahora combinando lectura vieja + escritura nueva dentro de la misma operación de borrado.
  *Mitigación:* el test de paridad del paso 5 corre contra estado limpio, sin operar simultáneamente sobre la misma fila desde ambos caminos en la misma transacción.

- **Condición de carrera entre el lookup de tipo y el borrado efectivo (TOCTOU).** Si otra request borra la misma pregunta entre que `EliminarPreguntaPorIdService` resuelve el tipo y despacha al `EliminarXxxUseCase`, el borrado delegado podría fallar con una excepción distinta a `BussinesException` (ej. `EmptyResultDataAccessException` desde Spring Data).
  *Mitigación:* ninguna en este spec — es el mismo nivel de garantía de concurrencia que ya tiene el resto de la aplicación (sin locking optimista/pesimista en ningún otro endpoint); no se introduce un riesgo nuevo, solo se hace explícito.

- **El `Map<TipoAResponder, EliminarXxxUseCase>` del orquestador podría quedar desactualizado si en el futuro se agrega un 7º tipo de pregunta** sin registrar su `EliminarXxxUseCase` en el map, causando que `DELETE /questions/{id}` falle silenciosamente (o con un error genérico) para ese tipo nuevo.
  *Mitigación:* el test de paridad del paso 5 cubre explícitamente los 6 tipos existentes; un tipo nuevo requeriría actualizar el map y el test, mismo criterio que "cuando agregues un nuevo tipo de pregunta debés tocar `FabricaDePreguntas`/`AsignadorDeTipoALasPreguntas`" ya documentado en `CLAUDE.md`.

- **El borrado en cascada de sub-entidades propias de cada tipo** (opciones de `SeleccionUnica`/`OpcionMultiple`, sub-preguntas de `DesplegableIndependiente`, opciones de `DesplegableCompartido`) depende de que el cascade JPA definido en las entities nuevas siga funcionando igual cuando se invoca desde este camino nuevo (antes esos 6 `EliminarXxxService` existían pero nunca se ejecutaban desde el controller real). Aunque el comportamiento ya fue validado en specs 02-06 de forma aislada, es la primera vez que se ejecuta desde el flujo real de la aplicación.
  *Mitigación:* el test de paridad del paso 5 verifica explícitamente que las sub-entidades también se borran para los tipos que las tienen; la verificación manual del paso 7 cubre el mismo caso contra `AQ-SIMPLE-FRONT`.

- **`AResponder.tipo` en el modelo JPA viejo es un campo mantenido a mano (no un discriminator de JPA)**, igual que ya se documentó en specs anteriores. Si esa columna quedara desincronizada para alguna fila existente (dato corrupto previo a este spec), el lookup despacharía al `EliminarXxxUseCase` equivocado o rechazaría un borrado válido.
  *Mitigación:* ninguna automática — mismo riesgo estructural ya aceptado en toda la migración, no introducido por este spec.
