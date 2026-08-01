# Spec 11 — Migración hexagonal: POST /questions/fetch y POST /questions/fetch-full (PreguntaSimple y VerdaderoOFalso)

**Estado:** Implementado
**Dependencias:** Specs 01-02 (hexagonal-content-piloto, hexagonal-verdadero-o-falso) — reutiliza `PreguntaSimpleRepositoryPort`/`PreguntaSimpleJpaAdapter` y `VerdaderoOFalsoRepositoryPort`/`VerdaderoOFalsoJpaAdapter` ya existentes (`findById`), sin modificarlos. Specs 03-06 (selección-única, opción-múltiple, desplegable-compartido, desplegable-independiente) — no se tocan; esos 4 tipos siguen sirviendo `/questions/fetch(-full)` desde `PreguntaService` (código viejo), sin cambios.
**Fecha:** 2026-08-01
**Objetivo:** Migrar `POST /questions/fetch` y `POST /questions/fetch-full` a arquitectura hexagonal para `PREGUNTA_SIMPLE` y `VERDADERO_FALSO` —los únicos dos tipos con uso real hoy en `AQ-SIMPLE-FRONT`— reemplazando la serialización basada en `@JsonView` del modelo viejo por DTOs planas nuevas por tipo y por vista, y corrigiendo en el proceso el leak de `respuestaVerdadera` en la vista `JustToAnswer`. Los otros 4 tipos de pregunta y la teoría de la pregunta quedan fuera, sirviéndose del código viejo sin cambios.

---

## Alcance

### Incluido

- **Application — DTOs de respuesta nuevas (planas, sin `@JsonView`):**
  - `PreguntaSimpleFetchDTO(Long id, String titulo, String descripcion, Long idDuenio, LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion, TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible, String imagenTitulo, Boolean respuestaPrecisa)` — sin `respuestaEstablecida`.
  - `PreguntaSimpleFullDTO` — mismos campos + `String respuestaEstablecida`.
  - `VerdaderoOFalsoFetchDTO(Long id, String titulo, String descripcion, Long idDuenio, LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion, TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible, String imagenTitulo)` — sin `respuestaVerdadera` (corrige el leak actual).
  - `VerdaderoOFalsoFullDTO` — mismos campos + `Boolean respuestaVerdadera`.
- **Application — orquestadores nuevos (uno por tipo y por vista, mismo criterio granular que specs 01-06):**
  - `ObtenerPreguntaUseCase`/`ObtenerPreguntaService` — `PreguntaSimpleFetchDTO obtener(Long id)`. Resuelve vía `PreguntaSimpleRepositoryPort.findById` (`BussinesException` si no existe), mapea a la DTO.
  - `ObtenerPreguntaFullUseCase`/`ObtenerPreguntaFullService` — `PreguntaSimpleFullDTO obtenerFull(Long id)`. Mismo patrón, incluye `respuestaEstablecida`.
  - `ObtenerVerdaderoOFalsoUseCase`/`ObtenerVerdaderoOFalsoService` — `VerdaderoOFalsoFetchDTO obtener(Long id)`. Resuelve vía `VerdaderoOFalsoRepositoryPort.findById`.
  - `ObtenerVerdaderoOFalsoFullUseCase`/`ObtenerVerdaderoOFalsoFullService` — `VerdaderoOFalsoFullDTO obtenerFull(Long id)`. Incluye `respuestaVerdadera`.
- **Dispatch en `PreguntaController`:** `POST /questions/fetch` y `POST /questions/fetch-full` agregan un `if/else` por `tipoAResponder` (mismo estilo ya usado en `createQuestion`/`updateQuestion`): `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` van a los 4 Use Cases nuevos; el resto (`SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`) sigue cayendo en `preguntaService.obtenerPregunta`/`obtenerPreguntaFull` (código viejo), sin cambios. Se mantiene `@JsonView` a nivel de método solo para el camino viejo (no afecta a las DTOs nuevas, que no tienen anotaciones de vista).
- **Tests:** por tipo (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO`) y por vista (fetch, fetch-full): caso feliz verificando los campos esperados presentes; caso explícito verificando que `respuestaEstablecida`/`respuestaVerdadera` NO aparecen en la vista fetch (ni siquiera como propiedad `respuestaCorrecta`); caso not-found (`BussinesException`). Test de que los otros 4 tipos siguen respondiendo exactamente igual que hoy (camino viejo intacto).

### Explícitamente NO incluido

- `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE` en `/questions/fetch(-full)` — siguen en código viejo tal cual, incluido el leak de `respuestaCorrecta` en esos 4 tipos (no se corrige en este spec).
- Teoría de la pregunta (`listaDeTeoriaDeLaPregunta`) — no se modela en el dominio nuevo; ningún DTO nuevo la incluye, para ninguno de los 6 tipos, ya que hoy no la consume ningún cliente real. Queda como candidato a un spec futuro si aparece un consumidor.
- `POST /questions/verify`, `POST /questions/inverse`, `ResponderController` (`random-ids`, `critical-ids`) — quedan para specs posteriores.
- Modificar `PreguntaService.obtenerPregunta`/`obtenerPreguntaFull` (viejo) — se reutilizan tal cual para los 4 tipos no migrados.
- Cualquier cambio de esquema de base de datos.
- Mover `PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT` — el contrato que el frontend realmente consume (`id`, `titulo`, `descripcion`, `respuestaEstablecida`, `idDuenio`) no cambia.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `content.application.port.out.PreguntaSimpleRepositoryPort` / `PreguntaSimpleJpaAdapter` (spec 01) — se reutiliza `findById(Long): Optional<PreguntaSimple>` sin modificarlo.
- `content.application.port.out.VerdaderoOFalsoRepositoryPort` / `VerdaderoOFalsoJpaAdapter` (spec 02) — se reutiliza `findById(Long): Optional<VerdaderoOFalso>` sin modificarlo.
- `content.domain.PreguntaSimple` (`respuestaEstablecida`, `respuestaPrecisa`) y `content.domain.VerdaderoOFalso` (`respuestaVerdadera`) — ya modelados, sin cambios.
- `content.domain.AResponder` (`id`, `titulo`, `descripcion`, `idDuenio`, `fechaDeCreacion`, `ultimaActualizacion`, `tipo`) y `content.domain.Pregunta` (`intentosParaQueDejeDeSerCriticoDisponible`, `imagenTitulo`) — sin cambios.
- `com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO(Long id, TipoAResponder tipoAResponder)` — DTO de request, mismo contrato, reutilizado tal cual por ambos endpoints.
- `com.lorenzomar3.AQ.exception.BussinesException` — mismo uso estándar ya establecido.
- `PreguntaService.obtenerPregunta`/`obtenerPreguntaFull` (viejo) — se reutilizan sin cambios para despachar `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`.

### `dto/newDto/` (nuevo)

- **`PreguntaSimpleFetchDTO`** — `record PreguntaSimpleFetchDTO(Long id, String titulo, String descripcion, Long idDuenio, LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion, TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible, String imagenTitulo, Boolean respuestaPrecisa)`.
- **`PreguntaSimpleFullDTO`** — mismos campos que `PreguntaSimpleFetchDTO` + `String respuestaEstablecida`.
- **`VerdaderoOFalsoFetchDTO`** — `record VerdaderoOFalsoFetchDTO(Long id, String titulo, String descripcion, Long idDuenio, LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion, TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible, String imagenTitulo)`.
- **`VerdaderoOFalsoFullDTO`** — mismos campos que `VerdaderoOFalsoFetchDTO` + `Boolean respuestaVerdadera`.

Ninguna tiene anotaciones `@JsonView`, `@JsonInclude` ni de JPA — son records planos, el filtrado de campos entre vista "fetch" y "full" es estructural (la DTO simplemente no declara el campo sensible), no declarativo.

### `content/application/port/in/` y `content/application/service/` (nuevo)

- **`ObtenerPreguntaUseCase`** — `PreguntaSimpleFetchDTO obtener(Long id)`.
- **`ObtenerPreguntaService`** — implementa el puerto; inyecta `PreguntaSimpleRepositoryPort`; resuelve vía `findById` (`BussinesException` si no existe); mapea a `PreguntaSimpleFetchDTO`.
- **`ObtenerPreguntaFullUseCase`** — `PreguntaSimpleFullDTO obtenerFull(Long id)`.
- **`ObtenerPreguntaFullService`** — mismo patrón, mapea a `PreguntaSimpleFullDTO` (incluye `respuestaEstablecida`).
- **`ObtenerVerdaderoOFalsoUseCase`** — `VerdaderoOFalsoFetchDTO obtener(Long id)`.
- **`ObtenerVerdaderoOFalsoService`** — implementa el puerto; inyecta `VerdaderoOFalsoRepositoryPort`; resuelve vía `findById` (`BussinesException` si no existe); mapea a `VerdaderoOFalsoFetchDTO`.
- **`ObtenerVerdaderoOFalsoFullUseCase`** — `VerdaderoOFalsoFullDTO obtenerFull(Long id)`.
- **`ObtenerVerdaderoOFalsoFullService`** — mismo patrón, mapea a `VerdaderoOFalsoFullDTO` (incluye `respuestaVerdadera`).

### `Controller/PreguntaController` (modificado)

- `getQuestion` (`POST /questions/fetch`): agrega dispatch por `tipoAResponder` — `PREGUNTA_SIMPLE` → `obtenerPreguntaUseCase.obtener(id)`; `VERDADERO_FALSO` → `obtenerVerdaderoOFalsoUseCase.obtener(id)`; cualquier otro tipo → `preguntaService.obtenerPregunta(id, tipo)` (viejo, sin cambios). Firma de retorno pasa de `ResponseEntity<Pregunta>` a `ResponseEntity<Object>` para acomodar los dos tipos de DTO nuevas junto con `Pregunta` (viejo) del resto de tipos.
- `getQuestionFull` (`POST /questions/fetch-full`): mismo criterio con `obtenerPreguntaFullUseCase`/`obtenerVerdaderoOFalsoFullUseCase` y fallback a `preguntaService.obtenerPreguntaFull(getQuestionDTO)`.
- `@JsonView(View.JustToAnswer.class)`/`@JsonView(View.Full.class)` a nivel de método se mantienen (siguen siendo necesarios para el camino viejo); no tienen efecto sobre las DTOs nuevas.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutilizan `pregunta`, `pregunta_simple` y `verdadero_o_falso` sin modificarlas.

---

## Plan de implementación

1. **DTOs de `PreguntaSimple`.** Crear `PreguntaSimpleFetchDTO` y `PreguntaSimpleFullDTO` en `dto/newDto/`, con los campos definidos en el modelo de datos.
2. **DTOs de `VerdaderoOFalso`.** Crear `VerdaderoOFalsoFetchDTO` y `VerdaderoOFalsoFullDTO` en `dto/newDto/`.
3. **Orquestadores de `PreguntaSimple`.** Crear `ObtenerPreguntaUseCase`/`ObtenerPreguntaService` y `ObtenerPreguntaFullUseCase`/`ObtenerPreguntaFullService` en `content.application.port.in`/`content.application.service`, inyectando `PreguntaSimpleRepositoryPort` y lanzando `BussinesException` si `findById` no encuentra el id.
4. **Orquestadores de `VerdaderoOFalso`.** Crear `ObtenerVerdaderoOFalsoUseCase`/`ObtenerVerdaderoOFalsoService` y `ObtenerVerdaderoOFalsoFullUseCase`/`ObtenerVerdaderoOFalsoFullService`, mismo patrón con `VerdaderoOFalsoRepositoryPort`.
5. **Test de contenido — `PreguntaSimple`.** Para `obtener`/`obtenerFull`: crear una `PreguntaSimple` con `respuestaEstablecida` seteada, verificar que `ObtenerPreguntaService.obtener` devuelve todos los campos comunes pero **sin** `respuestaEstablecida` (ni como propiedad `respuestaEstablecida` ni bajo ningún otro nombre), y que `ObtenerPreguntaFullService.obtenerFull` sí la incluye con el valor correcto.
6. **Test de contenido — `VerdaderoOFalso`.** Mismo criterio: `ObtenerVerdaderoOFalsoService.obtener` no incluye `respuestaVerdadera`; `ObtenerVerdaderoOFalsoFullService.obtenerFull` sí la incluye con el valor correcto.
7. **Tests de not-found.** Id inexistente en cada uno de los 4 Use Cases nuevos → `BussinesException`.
8. **Cablear `PreguntaController.getQuestion`.** Agregar el dispatch por `tipoAResponder`: `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` a los Use Cases nuevos, resto a `preguntaService.obtenerPregunta` (sin cambios). Cambiar la firma de retorno a `ResponseEntity<Object>`.
9. **Cablear `PreguntaController.getQuestionFull`.** Mismo dispatch con `obtenerPreguntaFullUseCase`/`obtenerVerdaderoOFalsoFullUseCase`, resto a `preguntaService.obtenerPreguntaFull` (sin cambios).
10. **Test de no regresión para los 4 tipos no migrados.** Para `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`: `POST /questions/fetch(-full)` sigue devolviendo exactamente lo mismo que antes de este spec (mismo camino viejo, sin cambios).
11. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: abrir/editar una `PreguntaSimple` y una `VerdaderoOFalso` desde `question-response-create-edit`, confirmar que `question-view`/`question-response` siguen mostrando `título`/`descripción`/`respuestaEstablecida` igual que antes (el contrato que el frontend realmente lee no cambió); confirmar que el resto del CRUD (`POST`/`PUT`/`DELETE /questions`, `GET /issues/{id}/items`, `POST`/`PUT`/`DELETE /issues`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [x] Existen `PreguntaSimpleFetchDTO` y `PreguntaSimpleFullDTO` en `dto/newDto/`, records planos sin `@JsonView`; `PreguntaSimpleFetchDTO` no declara `respuestaEstablecida`, `PreguntaSimpleFullDTO` sí.
- [x] Existen `VerdaderoOFalsoFetchDTO` y `VerdaderoOFalsoFullDTO` en `dto/newDto/`; `VerdaderoOFalsoFetchDTO` no declara `respuestaVerdadera`, `VerdaderoOFalsoFullDTO` sí.
- [x] Existen `ObtenerPreguntaUseCase`/`ObtenerPreguntaService` y `ObtenerPreguntaFullUseCase`/`ObtenerPreguntaFullService`, que resuelven vía `PreguntaSimpleRepositoryPort.findById` y lanzan `BussinesException` si el id no existe.
- [x] Existen `ObtenerVerdaderoOFalsoUseCase`/`ObtenerVerdaderoOFalsoService` y `ObtenerVerdaderoOFalsoFullUseCase`/`ObtenerVerdaderoOFalsoFullService`, que resuelven vía `VerdaderoOFalsoRepositoryPort.findById` y lanzan `BussinesException` si el id no existe.
- [x] `POST /questions/fetch` para una `PreguntaSimple` devuelve todos los campos comunes (`id`, `titulo`, `descripcion`, `idDuenio`, `fechaDeCreacion`, `ultimaActualizacion`, `tipo`, `intentosParaQueDejeDeSerCriticoDisponible`, `imagenTitulo`, `respuestaPrecisa`) pero **no** incluye `respuestaEstablecida` bajo ningún nombre.
- [x] `POST /questions/fetch-full` para una `PreguntaSimple` devuelve los mismos campos comunes **más** `respuestaEstablecida` con el valor correcto.
- [x] `POST /questions/fetch` para una `VerdaderoOFalso` devuelve todos los campos comunes pero **no** incluye `respuestaVerdadera` bajo ningún nombre — corrige el leak actual.
- [x] `POST /questions/fetch-full` para una `VerdaderoOFalso` devuelve los mismos campos comunes **más** `respuestaVerdadera` con el valor correcto.
- [x] `POST /questions/fetch(-full)` con un id inexistente de tipo `PREGUNTA_SIMPLE` o `VERDADERO_FALSO` lanza `BussinesException`.
- [x] `POST /questions/fetch(-full)` para `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO` y `DESPLEGABLE_INDEPENDIENTE` sigue devolviendo exactamente lo mismo que antes de este spec (código viejo intacto, sin cambios de comportamiento).
- [x] `PreguntaController.getQuestion`/`getQuestionFull` despachan por `tipoAResponder`: `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` a los Use Cases nuevos, el resto a `preguntaService.obtenerPregunta`/`obtenerPreguntaFull` (sin cambios).
- [x] El resto de endpoints (`POST`/`PUT`/`DELETE /questions`, `GET /issues/{id}/items`, CRUD de `Temario`/`Issue`) sigue funcionando sin cambios.
- [x] Existen tests cubriendo: contenido correcto por vista para ambos tipos migrados (incluida la ausencia explícita del campo sensible en la vista fetch), not-found para ambos tipos, y no regresión de los 4 tipos no migrados.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: editar/ver una `PreguntaSimple` y una `VerdaderoOFalso` desde `question-response-create-edit`/`question-view`/`question-response`, confirmando que se comportan igual que antes. **(verificado por el usuario)**
- [x] `PreguntaService.obtenerPregunta`/`obtenerPreguntaFull` (viejo) no fueron eliminados; siguen sirviendo a los 4 tipos no migrados.

---

## Decisiones tomadas y descartadas

- **Se migran solo `PREGUNTA_SIMPLE` y `VERDADERO_FALSO`; los otros 4 tipos siguen en código viejo.**
  Confirmado con el usuario, tras investigar que `AQ-SIMPLE-FRONT` hoy solo tiene UI real para esos dos tipos (`PreguntaSimple` completo, `VerdaderoOFalso` como stub sin lógica) — los otros 4 (`SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`) no tienen pantalla de edición ni el flujo de "responder" los soporta (está hardcodeado a `PREGUNTA_SIMPLE`). Descartado: migrar los 6 tipos con corrección de leak completa — se evaluó pero se consideró trabajo real para datos que hoy nadie lee, en contra del principio de no construir para requerimientos hipotéticos.

- **No se modela teoría de la pregunta (`listaDeTeoriaDeLaPregunta`) en el dominio nuevo.**
  Confirmado con el usuario, tras investigar que `content.domain.Pregunta` no tiene ningún campo de teoría hoy y que ningún componente de `AQ-SIMPLE-FRONT` la lee (se confirmó por grep exhaustivo sobre toda la app). Descartado: modelar teoría como ciudadano de dominio completo (clase, entity, mapper, puerto de solo lectura) — se llegó a diseñar como concepto independiente (no como campo anidado en `Pregunta`, para no arriesgar `orphanRemoval` en los mappers de crear/editar existentes) pero se abandonó por falta de consumidor real. Queda documentado como candidato a spec futuro si aparece una necesidad concreta.

- **Se reemplaza la serialización basada en `@JsonView` del modelo viejo por DTOs planas nuevas (sin `@JsonView`), una por tipo y por vista.**
  Confirmado con el usuario. Descartado: reutilizar el patrón ya usado en `PUT /questions` (mapear el dominio nuevo a las clases viejas `model.AResponder.TiposDePreguntas.Xxx` para heredar su `@JsonView`). Justificación: la investigación reveló que el mecanismo `@JsonView` actual tiene un bug estructural (`MapperFeature.DEFAULT_VIEW_INCLUSION` en `true` por defecto, sin ninguna configuración que lo deshabilite) que filtra la respuesta correcta en `JustToAnswer` para varios tipos; las DTOs planas eliminan esa clase de bug de raíz en vez de parchear anotaciones.

- **Se corrige el leak de `respuestaVerdadera` en `VerdaderoOFalso` (vista `JustToAnswer`), en vez de preservarlo por paridad estricta.**
  Confirmado con el usuario, desviación deliberada del criterio de "paridad exacta, no se corrigen bugs de negocio" usado en specs 07-10 — se justifica porque es un leak de la respuesta correcta (dato sensible), y porque `POST /questions/fetch` no lo ejercita hoy ningún cliente real (así que "corregirlo" no cambia ningún comportamiento observado en producción).

- **No se migra `POST /questions/fetch` con fix de `LazyInitializationException` como una decisión separada.**
  El fix queda implícito: al no incluir teoría en ninguna DTO nueva, el problema de carga perezosa sin inicializar (que afectaba a `listaDeTeoriaDeLaPregunta` en el camino viejo de `/questions/fetch`) desaparece estructuralmente para los dos tipos migrados, sin necesidad de tocar transacciones ni fetch-joins.

- **Dispatch por tipo dentro del controller (`if/else`), un Use Case por tipo y por vista, en vez de un orquestador único con `Map<TipoAResponder, ...>`.**
  Justificación: mismo estilo granular ya usado en `createQuestion`/`updateQuestion` del propio `PreguntaController` (donde conviven ramas migradas y no migradas), y en specs 01-06 (un Use Case por tipo). Se descartó imitar el patrón de `EliminarPreguntaPorIdService` (spec 09, `Map<TipoAResponder, EliminarXxxUseCase>`) porque ese orquestador despacha los 6 tipos con una firma de retorno común (`void`); acá cada tipo devuelve una DTO distinta, y solo 2 de 6 tipos están en alcance, así que un dispatcher genérico habría sido una abstracción prematura.

- **`ResponseEntity<Pregunta>` pasa a `ResponseEntity<Object>` en `getQuestion`/`getQuestionFull`.**
  Necesario porque el controller debe poder devolver tanto las DTOs nuevas (sin relación de tipos con `Pregunta`) como el `Pregunta` viejo (para los 4 tipos no migrados) desde el mismo método. Se descartó crear una interfaz marcadora común — con solo 2 tipos migrados y un fallback a una clase preexistente no relacionada, no se justifica la abstracción adicional.

- **`PreguntaService.obtenerPregunta`/`obtenerPreguntaFull` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores (07-10) — sigue sirviendo a los 4 tipos no migrados.

---

## Riesgos identificados

- **Coexistencia de dos formas de servir el mismo endpoint según el tipo.** `POST /questions/fetch(-full)` ahora responde con una DTO plana nueva para `PREGUNTA_SIMPLE`/`VERDADERO_FALSO`, y con el `Pregunta` viejo serializado vía `@JsonView` para los otros 4 tipos — dos formatos de respuesta distintos bajo la misma URL, diferenciados solo por el `tipoAResponder` del request. Mismo riesgo de "coexistencia de dos caminos" ya documentado en specs 01-10, ahora también visible en la forma de la respuesta, no solo en la fuente de datos.
  *Mitigación:* el test del paso 10 (no regresión de los 4 tipos viejos) cubre que el fallback no se rompió; el frontend no distingue formato de respuesta por tipo hoy (no tiene UI para los 4 tipos viejos), así que el riesgo es principalmente para un consumidor futuro de la API que no conozca esta asimetría.

- **`respuestaVerdadera` deja de filtrarse en `JustToAnswer` para `VerdaderoOFalso`, cambio de comportamiento observable respecto al código viejo (aunque sea la corrección de un bug).** Si en el futuro se construye la UI real de "responder" para `VERDADERO_FALSO` reutilizando `/questions/fetch` tal como está hoy pensado el flujo (mostrar la pregunta sin la respuesta antes de contestar), este spec ya deja ese camino correctamente cerrado — pero si algún código no detectado en la investigación dependiera del leak actual (poco probable, ya que ningún cliente real lo lee), dejaría de funcionar.
  *Mitigación:* la investigación confirmó por grep exhaustivo que ningún componente de `AQ-SIMPLE-FRONT` lee `respuestaVerdadera` de esta respuesta; el riesgo se considera bajo y aceptado explícitamente por el usuario.

- **Diferir teoría y los 4 tipos no migrados dejará una migración incompleta de `PreguntaController` por más tiempo.** A diferencia de `TemarioController` (100% migrado en spec 10), `PreguntaController` quedará con una mezcla más granular de ramas migradas/no migradas que puede ser más difícil de rastrear para quien retome el trabajo.
  *Mitigación:* las decisiones de alcance quedan documentadas explícitamente en este spec (sección de decisiones) como referencia para specs futuros que completen los 4 tipos restantes y/o teoría si aparece un consumidor real.

- **`AResponder.tipo` en el modelo JPA viejo es un campo mantenido a mano, no un discriminator de JPA** (mismo riesgo estructural ya documentado en specs anteriores) — si estuviera desincronizado para una fila existente, el dispatch por tipo del controller (nuevo `if/else`) podría enviar la request al Use Case nuevo equivocado o al camino viejo cuando debería ir al nuevo, sin que el sistema lo detecte.
  *Mitigación:* ninguna adicional — mismo riesgo estructural ya aceptado en toda la migración.

---

## Próximo spec sugerido

Con este spec, `PreguntaController` tiene migrados `fetch`/`fetch-full` (solo `PREGUNTA_SIMPLE`/`VERDADERO_FALSO`), `POST /questions`, `PUT /questions` y `DELETE /questions/{id}`. Siguen en código viejo: `POST /questions/verify`, `POST /questions/inverse`, y todo `ResponderController` (`POST /questions/random-ids`, `GET /questions/{id}/critical-ids`).

**Recomendación: migrar `ResponderController` a continuación (`POST /questions/random-ids` y `GET /questions/{id}/critical-ids`).** Es la migración más parecida a la de este spec: lectura pura, sin mutación de estado. Además, `random-ids` ya puede apoyarse casi directamente en `ObtenerIdsDePreguntasUseCase` (spec 08) para el caso `CUESTIONARIO`/`TEMA`/`SUBTEMA` — hoy `ResponderService.obtenerIdsDePreguntasDeManeraAleatoria` delega a `TemarioService.obtenerTodosLosIdsDePreguntas` (el equivalente viejo de ese mismo Use Case) —, minimizando el trabajo nuevo a un Use Case de shuffle sobre esa lista. `critical-ids` sí requiere un puerto nuevo (`getCriticsIdsForQuestion` no tiene hoy equivalente hexagonal), pero sigue siendo una lectura sin lógica de negocio riesgosa.

Se descarta arrancar por `POST /questions/verify` porque muta `intentosParaQueDejeDeSerCriticoDisponible` (lógica de "crítico") — más delicada, y ya señalada como candidata a su propio spec dedicado en la sugerencia de spec 10. Se descarta también `POST /questions/inverse` porque, a diferencia del resto, incluye lógica de transformación (`Jsoup.parse(...).text()` para stripear HTML) que conviene revisar en un spec propio en vez de combinarla con una migración de lectura.
