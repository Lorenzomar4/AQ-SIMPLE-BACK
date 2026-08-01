# Spec 12 — Migración hexagonal: `ResponderController`

**Estado:** Implementado
**Dependencias:** Specs 01-06 (repository ports por tipo de pregunta: `PreguntaSimpleRepositoryPort`, `VerdaderoOFalsoRepositoryPort`, `SeleccionUnicaRepositoryPort`, `OpcionMultipleRepositoryPort`, `DesplegableCompartidoRepositoryPort`, `DesplegableIndependienteRepositoryPort`) — se reutiliza `findById` de cada uno para el dispatch por tipo hoja. Spec 07 (`TemarioRepositoryPort`/`TemarioJpaAdapter`) — se le agrega un método nuevo. Spec 08 (`ObtenerIdsDePreguntasUseCase`/`Service`) — se reutiliza directamente sin modificar para el caso contenedor de `random-ids`. Spec 09 (`EliminarPreguntaPorIdService`) — se replica su patrón `Map<TipoAResponder, …>` para el dispatch por tipo.
**Fecha:** 2026-08-01
**Objetivo:** Migrar `POST /questions/random-ids` y `GET /questions/{id}/critical-ids` a arquitectura hexagonal: el primero reutiliza `ObtenerIdsDePreguntasUseCase` (spec 08) para el caso contenedor (`CUESTIONARIO`/`TEMA`/`SUBTEMA`) y migra completo el caso de tipo de pregunta hoja mediante dispatch por tipo sobre los repository ports existentes (specs 01-06); el segundo agrega un método nuevo a `TemarioRepositoryPort` que reutiliza la query nativa `getCriticsIdsForQuestion` sin reescribirla; en ambos casos el shuffle se unifica dentro del use case nuevo (el controller deja de hacerlo).

---

## Alcance

### Incluido

- **Application — nuevo método de puerto:**
  - `TemarioRepositoryPort.findCriticalQuestionIds(Long id): List<Long>` — implementado en `TemarioJpaAdapter`, delega en `aResponderRepositoryViejo.getCriticsIdsForQuestion(id)` (query nativa recursiva sin límite de profundidad) sin reescribirla. Método aparte de `findDirectChildren`/`findIssueItems` (specs 08/10) — semántica distinta (recursión completa sobre todo el subárbol, no limitada a 2 niveles).
- **Application — orquestadores nuevos:**
  - `ObtenerIdsAleatoriosDePreguntasUseCase`/`ObtenerIdsAleatoriosDePreguntasService` — `List<Long> obtenerIds(ObtenerPreguntaDTO dto)`. Caso contenedor (`CUESTIONARIO`/`TEMA`/`SUBTEMA`): delega en `ObtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(id)` (spec 08, sin modificarlo). Caso hoja (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO`, `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`): dispatch por `Map<TipoAResponder, …>` construido en `@PostConstruct` (mismo patrón que `EliminarPreguntaPorIdService`, spec 09) sobre los 6 `RepositoryPort` existentes (specs 01-06), llamando `findById(id)` para validar existencia (`BussinesException` si no existe) y devolviendo `List.of(id)`. En ambos casos, el use case hace `Collections.shuffle` sobre una copia mutable antes de devolver.
  - `ObtenerIdsCriticosUseCase`/`ObtenerIdsCriticosService` — `List<Long> obtenerIdsCriticos(Long id)`. Resuelve vía `TemarioRepositoryPort.findCriticalQuestionIds(id)` — **sin** validar existencia del id (silencioso, paridad exacta con el comportamiento actual). Hace `Collections.shuffle` sobre una copia mutable antes de devolver.
- **Dispatch en `ResponderController`:** `obtenerListaDeIdsDePreguntas` (`POST /questions/random-ids`) delega en `ObtenerIdsAleatoriosDePreguntasUseCase.obtenerIds`; `obtenerIdsCriticos` (`GET /questions/{id}/critical-ids`) delega en `ObtenerIdsCriticosUseCase.obtenerIdsCriticos`. Se elimina el `Collections.shuffle`/`ArrayList` del controller (ya no le corresponde, el shuffle vive en los use cases nuevos) y sus imports si quedan sin uso.
- **Tests:**
  - Paridad para el caso contenedor de `random-ids` contra `ResponderService.obtenerIdsDePreguntasDeManeraAleatoria` (viejo): mismos ids, sin importar orden.
  - Paridad para cada uno de los 6 tipos hoja de `random-ids`: devuelve `List.of(id)` cuando la pregunta existe.
  - Not-found para `random-ids`: id inexistente en caso contenedor y en cada tipo hoja → `BussinesException`.
  - Paridad para `critical-ids` contra `ResponderService.obtenerCriticosDeManeraAleatoria` (viejo): árbol con varios niveles de profundidad (más de 2, para probar que no hay límite de nivel) y mezcla de preguntas críticas/no críticas — mismos ids, sin importar orden.
  - Caso `critical-ids` con id inexistente: devuelve lista vacía (sin excepción), documentando el comportamiento silencioso preservado.

### Explícitamente NO incluido

- Modificar `ResponderService` (viejo) — se reutiliza tal cual como baseline de los tests de paridad; no se elimina ni se toca.
- Agregar validación de existencia a `critical-ids` — se mantiene el comportamiento silencioso actual (lista vacía si el id no existe), decisión ya cerrada con el usuario.
- Reescribir la query nativa `getCriticsIdsForQuestion` o cambiar su alcance (recursión completa sin límite de profundidad) — se reutiliza tal cual.
- Modificar `AResponderChildRef`, `AResponderItemDetail`, `findDirectChildren` o `findIssueItems` (specs 08/10) — no se tocan; `findCriticalQuestionIds` es un método de puerto independiente.
- Modificar `ObtenerIdsDePreguntasUseCase`/`Service` (spec 08) — se reutiliza sin cambios para el caso contenedor.
- `POST /questions/verify`, `POST /questions/inverse` — quedan para specs posteriores.
- Cualquier cambio de esquema de base de datos.
- Mover `ResponderController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `content.application.port.in.ObtenerIdsDePreguntasUseCase` / `content.application.service.ObtenerIdsDePreguntasService` (spec 08) — se reutiliza `obtenerIdsDePreguntas(Long id): List<Long>` sin modificarlo, para el caso contenedor de `random-ids`.
- `content.application.port.out.PreguntaSimpleRepositoryPort`, `VerdaderoOFalsoRepositoryPort`, `SeleccionUnicaRepositoryPort`, `OpcionMultipleRepositoryPort`, `DesplegableCompartidoRepositoryPort`, `DesplegableIndependienteRepositoryPort` (specs 01-06) — se reutiliza `findById(Long): Optional<T>` de cada uno para validar existencia en el caso hoja, sin modificarlos.
- `com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO(Long id, TipoAResponder tipoAResponder)` — mismo contrato, reutilizado tal cual como request de `random-ids`.
- `com.lorenzomar3.AQ.exception.BussinesException`, `com.lorenzomar3.AQ.model.TipoAResponder` — mismo uso estándar ya establecido.
- `Repository.AResponderRepository.getCriticsIdsForQuestion(Long id): ArrayList<Long>` (query nativa, viejo) — se reutiliza tal cual desde `TemarioJpaAdapter`, que ya inyecta `AResponderRepository` (`aResponderRepositoryViejo`, desde spec 08).
- `Service.ResponderService` (viejo) — se reutiliza tal cual como baseline de los tests de paridad; no se elimina ni se modifica.

### `content/application/port/out/TemarioRepositoryPort` (modificado)

- Se agrega `List<Long> findCriticalQuestionIds(Long id)` — devuelve todos los ids de pregunta "críticos" (`intentosParaQueDejeDeSerCriticoDisponible > 0`) bajo el subárbol de `id`, sin límite de profundidad ni validación de existencia (si `id` no existe, la query nativa devuelve lista vacía).

### `content/infrastructure/persistence/adapter/TemarioJpaAdapter` (modificado)

- Implementa `findCriticalQuestionIds` delegando en `aResponderRepositoryViejo.getCriticsIdsForQuestion(id)` — el `ArrayList<Long>` devuelto se retorna directamente como `List<Long>`, sin transformación ni copia adicional.

### `content/application/port/in/` y `content/application/service/` (nuevo)

- **`ObtenerIdsAleatoriosDePreguntasUseCase`** — `List<Long> obtenerIds(ObtenerPreguntaDTO dto)`.
- **`ObtenerIdsAleatoriosDePreguntasService`** — implementa el puerto; inyecta `ObtenerIdsDePreguntasUseCase` y los 6 `RepositoryPort` de tipos hoja. Constante local `TIPOS_CONTENEDOR = List.of(CUESTIONARIO, TEMA, SUBTEMA)` (mismo criterio de duplicación ya usado en `ObtenerIdsDePreguntasService`). En `@PostConstruct` arma `Map<TipoAResponder, Function<Long, Boolean>>` con una entrada por tipo hoja, cada una `id -> xxxRepositoryPort.findById(id).isPresent()`. Lógica de `obtenerIds`: si `dto.tipoAResponder()` ∈ `TIPOS_CONTENEDOR` → delega en `obtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(dto.id())` (ya lanza `BussinesException` si no existe); si no, busca la función en el `Map` (`BussinesException` si el tipo no está registrado), la ejecuta (`BussinesException` si devuelve `false`) y arma `List.of(dto.id())`. En ambos casos, copia el resultado a una lista mutable y hace `Collections.shuffle` antes de devolver.
- **`ObtenerIdsCriticosUseCase`** — `List<Long> obtenerIdsCriticos(Long id)`.
- **`ObtenerIdsCriticosService`** — implementa el puerto; inyecta `TemarioRepositoryPort`. Resuelve vía `findCriticalQuestionIds(id)` (sin validar existencia), copia el resultado a una lista mutable y hace `Collections.shuffle` antes de devolver.

### `Controller/ResponderController` (modificado)

- `obtenerListaDeIdsDePreguntas` (`POST /questions/random-ids`): delega en `obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(obtenerPreguntaDTO)` y devuelve el resultado directamente — ya no hace `Collections.shuffle` ni maneja `ArrayList` él mismo.
- `obtenerIdsCriticos` (`GET /questions/{id}/critical-ids`): delega en `obtenerIdsCriticosUseCase.obtenerIdsCriticos(id)` y devuelve el resultado directamente — ya no hace `Collections.shuffle`.
- Imports `java.util.ArrayList`/`java.util.Collections` se eliminan si quedan sin uso.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutiliza `aresponder`/`pregunta` tal cual, misma query nativa `getCriticsIdsForQuestion`.

---

## Plan de implementación

1. **Extender el puerto.** Agregar `List<Long> findCriticalQuestionIds(Long id)` a `TemarioRepositoryPort`.
2. **Implementar en el adapter.** En `TemarioJpaAdapter`, implementar `findCriticalQuestionIds` delegando en `aResponderRepositoryViejo.getCriticsIdsForQuestion(id)`.
3. **Orquestador de críticos.** Crear `ObtenerIdsCriticosUseCase` (`content.application.port.in`) y `ObtenerIdsCriticosService`: inyecta `TemarioRepositoryPort`, resuelve vía `findCriticalQuestionIds(id)`, copia a lista mutable y hace `Collections.shuffle` antes de devolver.
4. **Orquestador de random-ids.** Crear `ObtenerIdsAleatoriosDePreguntasUseCase` y `ObtenerIdsAleatoriosDePreguntasService`: inyecta `ObtenerIdsDePreguntasUseCase` (spec 08) y los 6 `RepositoryPort` de tipos hoja; construye en `@PostConstruct` el `Map<TipoAResponder, Function<Long, Boolean>>` de existencia por tipo hoja; dispatch por `TIPOS_CONTENEDOR` vs. tipo hoja como se definió en el modelo de datos; copia a lista mutable y hace `Collections.shuffle` antes de devolver.
5. **Test de paridad — random-ids, caso contenedor.** Árbol con `CUESTIONARIO`/`TEMA`/`SUBTEMA` y preguntas mixtas: comparar `ObtenerIdsAleatoriosDePreguntasService.obtenerIds` contra `ResponderService.obtenerIdsDePreguntasDeManeraAleatoria` (viejo) — mismos ids, sin importar orden.
6. **Test de paridad — random-ids, tipos hoja.** Para cada uno de los 6 tipos: pregunta existente → devuelve `List.of(id)`, igual que el camino viejo.
7. **Tests not-found — random-ids.** Id inexistente en caso contenedor y en cada uno de los 6 tipos hoja → `BussinesException`.
8. **Test de paridad — critical-ids.** Árbol de más de 2 niveles de profundidad con mezcla de preguntas críticas y no críticas: comparar `ObtenerIdsCriticosService.obtenerIdsCriticos` contra `ResponderService.obtenerCriticosDeManeraAleatoria` (viejo) — mismos ids, sin importar orden.
9. **Test — critical-ids con id inexistente.** Devuelve lista vacía, sin excepción (comportamiento silencioso preservado).
10. **Cablear `ResponderController`.** `obtenerListaDeIdsDePreguntas` delega en `obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds`; `obtenerIdsCriticos` delega en `obtenerIdsCriticosUseCase.obtenerIdsCriticos`; se elimina el shuffle y los imports `ArrayList`/`Collections` si quedan sin uso.
11. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: iniciar el flujo de "responder" un cuestionario/tema (`getQuestionResponseIdList`) y el flujo de "responder críticos" (`getQuestionResponseIdListCritic`), confirmando que ambos siguen devolviendo la lista de preguntas esperada y que el resto de endpoints (`fetch(-full)`, CRUD de `issues`/`questions`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [x] `TemarioRepositoryPort` expone `findCriticalQuestionIds(Long id)`; `TemarioJpaAdapter` lo implementa reutilizando `AResponderRepository.getCriticsIdsForQuestion` (viejo), sin duplicar la query nativa.
- [x] Existen `ObtenerIdsCriticosUseCase`/`ObtenerIdsCriticosService`, que resuelven vía `TemarioRepositoryPort.findCriticalQuestionIds` sin validar existencia del id, y devuelven la lista con `Collections.shuffle` aplicado.
- [x] Existen `ObtenerIdsAleatoriosDePreguntasUseCase`/`ObtenerIdsAleatoriosDePreguntasService`, que para `CUESTIONARIO`/`TEMA`/`SUBTEMA` delegan en `ObtenerIdsDePreguntasUseCase` (spec 08) y para los 6 tipos hoja validan existencia vía el `RepositoryPort` correspondiente (specs 01-06), lanzando `BussinesException` si el id no existe o el tipo no está soportado; devuelven la lista con `Collections.shuffle` aplicado.
- [x] `POST /questions/random-ids` para un `CUESTIONARIO`/`TEMA`/`SUBTEMA` devuelve el mismo conjunto de ids que el camino viejo (`ResponderService.obtenerIdsDePreguntasDeManeraAleatoria`), sin importar orden.
- [x] `POST /questions/random-ids` para cada uno de los 6 tipos hoja (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO`, `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`) con id existente devuelve `[id]`.
- [x] `POST /questions/random-ids` con un id inexistente (en cualquiera de los tipos soportados, contenedor u hoja) lanza `BussinesException`.
- [x] `GET /questions/{id}/critical-ids` devuelve el mismo conjunto de ids que el camino viejo (`ResponderService.obtenerCriticosDeManeraAleatoria`) para un árbol de más de 2 niveles de profundidad con preguntas críticas y no críticas mezcladas, sin importar orden.
- [x] `GET /questions/{id}/critical-ids` con un id inexistente devuelve lista vacía, sin excepción (comportamiento silencioso preservado).
- [x] `ResponderController.obtenerListaDeIdsDePreguntas`/`obtenerIdsCriticos` delegan en los use cases nuevos y ya no hacen `Collections.shuffle` ellos mismos.
- [x] El resto de endpoints (`POST`/`PUT`/`DELETE /questions`, `POST /questions/fetch(-full)`, CRUD de `Temario`/`Issue`) sigue funcionando sin cambios.
- [x] Existen tests cubriendo: paridad de `random-ids` (caso contenedor y los 6 tipos hoja), not-found de `random-ids` (contenedor y hoja), paridad de `critical-ids` con árbol de más de 2 niveles, y `critical-ids` con id inexistente.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: iniciar el flujo de "responder" (`getQuestionResponseIdList`) y el flujo de "responder críticos" (`getQuestionResponseIdListCritic`), confirmando que ambos se comportan igual que antes. **(verificado por el usuario)**
- [x] `ResponderService` (viejo) no fue eliminado; queda sin uso desde el controller, como baseline de los tests de paridad.

---

## Decisiones tomadas y descartadas

- **Se migra completo el caso de tipos hoja de `random-ids` con dispatch por tipo, en vez de delegar al código viejo (`PreguntaService.obtenerTodosLosIdsDePreguntas`).**
  Confirmado con el usuario. Descartado: reutilizar el camino viejo tal cual para esos 6 tipos (mismo criterio que spec 11 usó para los 4 tipos no migrados de `fetch(-full)`) — se evaluó como opción de menor esfuerzo, dado que esa rama solo valida existencia y devuelve una lista de un elemento, pero el usuario prefirió dejar el endpoint 100% hexagonal: los 6 `RepositoryPort` necesarios ya existen desde specs 01-06, así que no había trabajo de infraestructura nuevo que justificara diferirlo.

- **Se unifica el shuffle dentro de los use cases nuevos, en vez de preservar la inconsistencia actual (shuffle en el controller para `random-ids`, en el service para `critical-ids`).**
  Confirmado con el usuario. Descartado: mantener el shuffle repartido tal cual estaba, por ser "paridad más literal" a nivel de ubicación de código. Justificación: el resultado observable (lista con orden aleatorio) es idéntico de cualquier forma — no es un cambio de comportamiento, solo de dónde vive el código — y unificarlo deja a ambos use cases nuevos siguiendo el mismo criterio interno.

- **`critical-ids` mantiene su comportamiento silencioso ante un id inexistente (lista vacía, sin `BussinesException`).**
  Confirmado con el usuario. Descartado: agregar validación explícita de existencia, como hicieron los specs 10/11 al corregir comportamientos de negocio durante la migración. Se descartó porque acá no hay ningún leak de dato sensible ni bug de seguridad de por medio (a diferencia del leak de respuesta correcta corregido en spec 11) — es simplemente un caso silencioso preexistente sin evidencia de que cause problemas reales, así que se prioriza no tocar comportamiento fuera del alcance estrictamente necesario de esta migración.

- **Dispatch por tipo hoja mediante `Map<TipoAResponder, Function<Long, Boolean>>` construido en `@PostConstruct`, replicando el patrón de `EliminarPreguntaPorIdService` (spec 09), en vez de un `if/else` (como en `createQuestion`/`updateQuestion` de `PreguntaController`) o de reutilizar `AResponderTipoLookupPort`.**
  Justificación: el tipo ya viene dado en el request (`ObtenerPreguntaDTO.tipoAResponder()`), a diferencia de `EliminarPreguntaPorIdService`, que necesita descubrir el tipo primero vía `AResponderTipoLookupPort` — ese lookup no hace falta acá. Se prefirió el patrón `Map` sobre `if/else` porque las 6 ramas son triviales y homogéneas (todas "existe por id"), a diferencia del `if/else` de `PreguntaController`, donde cada rama construye/mapea una entidad distinta y sí justifica ramas explícitas.

- **`findCriticalQuestionIds` expone `List<Long>` en la firma del puerto, no `ArrayList<Long>`**, aunque el adapter reciba internamente un `ArrayList` de la query nativa.
  Justificación: mismo criterio ya usado en el resto de `TemarioRepositoryPort` (`findAllCuestionarios`, `findDirectChildren`, `findIssueItems` — todos devuelven `List<T>`, nunca un tipo concreto de colección).

- **No se crea un DTO de response nuevo para ninguno de los dos endpoints** — ambos siguen devolviendo `List<Long>` crudo (JSON array de números), igual que hoy.
  Justificación: mismo contrato de wire exacto; envolver un array de longs en un DTO hubiera sido una abstracción innecesaria.

- **`TIPOS_CONTENEDOR` se duplica como constante local en `ObtenerIdsAleatoriosDePreguntasService`**, en vez de extraerla a un lugar compartido con `ObtenerIdsDePreguntasService` (que ya define la misma constante).
  Mismo criterio de no crear abstracciones compartidas prematuras ya aplicado en toda la migración (p. ej. `AResponderChildRef` vs. `AResponderItemDetail` en spec 10, con forma parecida pero sin unificar).

- **`ResponderService` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores (07-11) — sigue como baseline de los tests de paridad.

---

## Riesgos identificados

- **Coexistencia de dos formas de resolver la misma lógica.** `ResponderService` (viejo) queda intacto en el codebase pero sin ningún caller activo, mientras `ResponderController` pasa a depender enteramente de los use cases nuevos. Mismo patrón de coexistencia ya documentado en specs 07-11, ahora aplicado a un service completo en vez de a un método puntual.
  *Mitigación:* los tests de paridad (pasos 5, 6, 8) usan `ResponderService` como baseline explícito, así que su código sigue ejercitado por los tests aunque no por el tráfico real.

- **El `Map<TipoAResponder, Function<Long, Boolean>>` de `ObtenerIdsAleatoriosDePreguntasService` requiere registro manual por tipo.** Si en el futuro se agrega un séptimo tipo de pregunta sin agregar su entrada al `Map`, `random-ids` lanzaría `BussinesException` ("tipo no soportado") para ese tipo en vez de funcionar — un punto de registro más que sumar a los ya documentados en `CLAUDE.md` (subclase de `Pregunta`, `FabricaDePreguntas`, `AsignadorDeTipoALasPreguntas`, repositorio nuevo).
  *Mitigación:* ninguna adicional en este spec — mismo riesgo estructural de "registro en múltiples puntos" ya aceptado en el resto de la arquitectura; queda documentado acá para quien agregue un tipo nuevo.

- **El dispatch de `random-ids` confía en el `tipoAResponder` que envía el cliente en el request (`ObtenerPreguntaDTO`), no en el tipo real almacenado en la fila.** Si un cliente envía un `id` real pero con el `tipoAResponder` equivocado (p. ej. un id de `VERDADERO_FALSO` pedido como `PREGUNTA_SIMPLE`), el `RepositoryPort` del tipo declarado no lo va a encontrar (la herencia `JOINED` hace que ese id no tenga fila en la tabla del tipo equivocado) y el endpoint responde `BussinesException` como si el id no existiera, aunque sí exista bajo otro tipo.
  *Mitigación:* ninguna nueva — es el mismo comportamiento que ya tiene el camino viejo (`PreguntaService.obtenerPregunta` también despacha por el tipo declarado en el request, no por lookup); no es una regresión introducida por este spec.

- **`findCriticalQuestionIds` expone como método de puerto genérico una recursión SQL sin límite de profundidad**, a diferencia de `findDirectChildren`/`findIssueItems` (specs 08/10), que sí están limitados a 2 niveles. Un futuro consumidor del puerto podría asumir erróneamente que todos los métodos de `TemarioRepositoryPort` tienen el mismo costo/alcance.
  *Mitigación:* ninguna en este spec — la query nativa reutilizada es la misma que ya corre hoy en producción sin límite de profundidad; el riesgo de rendimiento sobre árboles muy grandes ya existe independientemente de esta migración. Queda documentado como asimetría a tener en cuenta si se agregan más métodos a este puerto.

- **`AResponder.tipo` en el modelo JPA viejo sigue siendo un campo mantenido a mano, no un discriminator de JPA** (mismo riesgo estructural ya documentado en specs anteriores) — si estuviera desincronizado para una fila existente, tanto `findCriticalQuestionIds` (que filtra por `JOIN` con `pregunta`, no por `tipo`) como el dispatch de tipos contenedor en `ObtenerIdsAleatoriosDePreguntasService` podrían comportarse de forma inconsistente con lo que el usuario espera ver.
  *Mitigación:* ninguna adicional — mismo riesgo estructural ya aceptado en toda la migración.

---

## Hallazgo durante la verificación: bug de Jackson (records + `@JsonView`) en `PreguntaController`

Durante la verificación manual del flujo de "responder" contra `AQ-SIMPLE-FRONT` apareció un bug bloqueante en `POST /questions/fetch-full`: para `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` la respuesta llegaba vacía (`{}`), aunque el status era `200 OK`. No lo causó este spec — vive en `PreguntaController.getQuestion`/`getQuestionFull` (spec 11), que este spec no toca; nunca se había detectado porque los tests de spec 11 serializan las DTOs con `objectMapper.writeValueAsString(dto)` sin vista activa, evitando el camino que dispara el bug. Como el flujo de "responder" migrado en este spec fue lo que por primera vez ejercitó `fetch-full` de punta a punta contra el frontend real, quedó bloqueando la verificación del Paso 11 y se corrigió en esta misma rama con autorización del usuario.

**Causa raíz confirmada:** `jackson-databind` no respeta `MapperFeature.DEFAULT_VIEW_INCLUSION` para los componentes de un `record` cuando hay una vista (`@JsonView`) activa — a diferencia de una clase normal, donde una propiedad sin anotación de vista se incluye igual por default, en un `record` cualquier vista activa excluye **todas** las propiedades que no tengan `@JsonView` explícito. Se confirmó con un test puntual (`objectMapper.writerWithView(View.Full.class).writeValueAsString(dto)` sobre `PreguntaSimpleFullDTO`) que reprodujo `{}` incluso en `jackson-databind 2.21.4` — el comportamiento es independiente de la versión, no un bug puntual con fix conocido.

**Cambios aplicados (fuera del alcance original de este spec):**
- `pom.xml`: `spring-boot-starter-parent` `3.1.5` → `3.5.16` (última versión libre de la serie 3.x; toda la 3.x quedó sin soporte el 30/06/2026). Decisión del usuario: mantener este upgrade aunque no resolvió el bug por sí solo, en vez de revertirlo.
- `PreguntaController.getQuestion`/`getQuestionFull`: se quita `@JsonView(...)` a nivel de método (eso activaba la vista sobre toda la respuesta, incluidas las DTOs nuevas). Para los tipos migrados (`PREGUNTA_SIMPLE`/`VERDADERO_FALSO`) las DTOs nuevas se devuelven sin vista activa. Para los 4 tipos viejos no migrados, el `Pregunta` viejo se envuelve en `MappingJacksonValue` con `setSerializationView(...)`, preservando el filtrado (`JustToAnswer`/`Full`) que ya tenían.

**Verificado por el usuario:** `./mvnw test` completo pasa con la versión nueva de Spring Boot/Jackson; `/questions/fetch-full` funciona correctamente para `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` desde el flujo de "responder" y "responder críticos".

**Pendiente para un spec futuro:** este fix no toca los 4 tipos de pregunta no migrados (`SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`), que siguen sirviéndose desde el `Pregunta` viejo vía `MappingJacksonValue` — sin cambios de comportamiento para ellos. Tampoco se evaluó si el mismo patrón de bug (`record` + vista activa) puede afectar otros endpoints con DTOs nuevas fuera de `PreguntaController`.

---

## Próximo spec sugerido

Con este spec, `TemarioController` y `ResponderController` quedan 100% migrados. En `PreguntaController` siguen sin migrar `POST /questions/verify` y `POST /questions/inverse` (individual).

**Recomendación: migrar `POST /questions/verify` a continuación.** Es el único camino de escritura que queda sin migrar en el flujo de "responder" — muta `intentosParaQueDejeDeSerCriticoDisponible` (lógica de "crítico": incorrecta → 3, correcta → decrementar si > 0) vía `verificarSiLaRespuestaEsCorrectaYAsignarCriticos`, delegando la corrección de la respuesta en `laRespuestaEsCorrecta` de cada subclase de `Pregunta`. Dado que este spec ya migró `PREGUNTA_SIMPLE`/`VERDADERO_FALSO` en `fetch(-full)` (spec 11) y ahora los 6 tipos en `random-ids`, `verify` es el siguiente candidato natural para consolidar el flujo de "responder" completo en hexagonal, al menos para esos dos tipos con uso real en `AQ-SIMPLE-FRONT`.

Se descarta `POST /questions/inverse` porque incluye lógica de transformación (`Jsoup.parse(...).text()` para stripear HTML) que conviene revisar en un spec propio en vez de combinarla con la migración de "crítico", más delicada por la mutación de estado.
