# Spec 10 — Migración hexagonal: `GET /issues/{id}/items`

**Estado:** Implementado
**Dependencias:** Spec 07 (hexagonal-issue-crud-basico) — reutiliza `TemarioRepositoryPort`/`TemarioJpaAdapter` y el patrón de inyectar `AResponderRepository` viejo dentro del adapter nuevo; Spec 08 (hexagonal-issue-question-ids-inverse) — mismo patrón de reutilizar la query nativa `getIssueItems`, pero con un método de puerto nuevo (no `findDirectChildren`, que descarta los campos que este endpoint necesita).
**Fecha:** 2026-08-01
**Objetivo:** Migrar `GET /issues/{id}/items` a arquitectura hexagonal reutilizando la query nativa `AResponderRepository.getIssueItems` a través de un método nuevo en `TemarioRepositoryPort`, preservando el contrato de wire y la lógica de negocio exactos (incluida la limitación de 2 niveles de profundidad), pero reemplazando el manejo de errores "fail-silently" actual por propagación directa de excepciones.

---

## Alcance

### Incluido

- **Application — nuevo record interno más rico:**
  - `AResponderItemDetail` (`content.application.port.out`, junto a `AResponderChildRef`) — `record AResponderItemDetail(Long id, TipoAResponder tipo, String titulo, LocalDateTime fechaDeCreacion, Boolean esCritico, Integer numeroDePreguntas)`. Análogo a `AResponderChildRef` pero con todos los campos que trae la query vieja (`QuestionnaireItem`), en vez de solo `id`+`tipo`. Puramente interno a `content.application`, sin anotaciones de Spring/JPA/Jackson.
- **Application — nuevo método de puerto:**
  - `TemarioRepositoryPort` — se agrega `List<AResponderItemDetail> findIssueItems(Long id)`. Devuelve la raíz (`id` pedido) + todo lo que hoy trae `AResponderRepository.getIssueItems` (hijos directos y, para contenedores de nivel 2, sus métricas agregadas) — misma semántica y mismo límite de 2 niveles que hoy.
  - `TemarioJpaAdapter` — implementa `findIssueItems` inyectando el `AResponderRepository` viejo (ya inyectado desde spec 08) y delegando en `getIssueItems(id)`, mapeando cada `QuestionnaireItem` a `AResponderItemDetail`. No se toca `findDirectChildren` (spec 08), que sigue existiendo tal cual para sus propios use cases.
- **Application — orquestador del endpoint:**
  - `ObtenerItemsDeIssueUseCase` (`content.application.port.in`) — `IssueWhitItemsDTO obtenerItems(Long id)`.
  - `ObtenerItemsDeIssueService` — resuelve la raíz vía `TemarioRepositoryPort.findById(id)` (`BussinesException` si no existe, mismo patrón que el resto de servicios de spec 07/08/09); obtiene la lista completa vía `findIssueItems(id)`; extrae la fila de la raíz (`filter` por id + `findFirst().get()`) para el `isCritic` del issue; arma el resto como `itemList` (`filter` excluyendo la raíz). **Sin try/catch**: cualquier excepción (incluida `NoSuchElementException` si la raíz no apareciera en la lista) se propaga sin envolver — decisión ya cerrada, reemplaza el "fail-silently" viejo.
- **Wire — nuevo DTO para los items de la lista:**
  - `IssueItemDTO` — `record IssueItemDTO(Long id, String name, TipoAResponder type, LocalDateTime creationDate, Boolean isCritic, Integer numberOfQuestions)`. Reemplaza a `QuestionnaireItem` como tipo de los elementos de `itemList` en la respuesta de este endpoint — mismo shape JSON (Jackson serializa records por nombre de componente, igual que ya hace `AResponderItemListDTO`).
  - `IssueWhitItemsDTO` (existente) se reutiliza tal cual en su forma (`id`, `name`, `creationDate`, `fatherid`, `itemList`, `type`, `isCritic`), solo cambiando el tipo genérico de `itemList` al DTO nuevo en vez de `QuestionnaireItem`.
- **Dispatch en `TemarioController`:** `GET /issues/{id}/items` (`getTopicContent`) delega a `ObtenerItemsDeIssueUseCase` en vez de `preguntaService.getIssueItems`.
- **Tests:**
  - Test de paridad (camino feliz) contra `PreguntaService.getIssueItems` (viejo): árbol con hijos directos mixtos (preguntas + subtemas) comparando raíz (`id`, `name`, `creationDate`, `fatherid`, `type`, `isCritic`) e `itemList` (mismos ids, `name`, `type`, `creationDate`, `isCritic`, `numberOfQuestions`, sin importar orden).
  - Test de caso not-found: id inexistente → `BussinesException`.
  - Test unitario (con `TemarioRepositoryPort` mockeado) que fuerza el escenario donde la raíz no aparece en `findIssueItems(id)`, verificando que la excepción se propaga (ya no hay fallback a lista vacía) — cubre el cambio de comportamiento decidido, que no es reproducible con datos reales.

### Explícitamente NO incluido

- Modificar `AResponderRepository.getIssueItems`, la projection `QuestionnaireItem`, o `IssueOrQuestionnaireProjection` — se reutilizan tal cual, incluida la limitación de 2 niveles de profundidad en `isCritic`/`numberOfQuestions` (confirmado, no se corrige en este spec).
- Corregir las inconsistencias preexistentes del contrato de wire (`type` string vs enum, `fatherid` vs `lastUpdateDate` del frontend) — confirmado, paridad exacta.
- `AResponderItemListDTO` — DTO distinto usado por `POST /issues` y `POST /issues/inverse` (spec 07/08); no se toca.
- `findDirectChildren`/`AResponderChildRef` (spec 08) — no se modifican; el nuevo `findIssueItems` es un método aparte.
- `PreguntaService.getIssueItems` (viejo) — no se elimina; queda como baseline del test de paridad, mismo criterio que specs anteriores.
- Cualquier cambio de esquema de base de datos.
- Mover `TemarioController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- `POST /questions/fetch(-full)`, `POST /questions/verify`, `POST /questions/inverse` (individual), `ResponderController` — quedan para specs posteriores.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `Repository.AResponderRepository.getIssueItems(Long id)` y la projection `projections.QuestionnaireItem` (código viejo) — reutilizados desde `TemarioJpaAdapter`, sin modificarlos.
- `Repository.TemarioRepository.findByIdBasic` / `IssueOrQuestionnaireProjection` — **dejan de usarse en este endpoint** (se reemplazan por `TemarioRepositoryPort.findById`, que ya expone los mismos datos de la raíz vía `content.domain.Temario`), pero no se tocan ni se eliminan — pueden seguir en uso desde otro lado del código viejo.
- `TemarioRepositoryPort.findById(Long id): Optional<Temario>` (spec 07) — se reutiliza tal cual para resolver la raíz (`id`, `titulo`→`name`, `fechaDeCreacion`→`creationDate`, `idDuenio`→`fatherid`, `tipo`→`type`).
- `com.lorenzomar3.AQ.model.TipoAResponder`, `com.lorenzomar3.AQ.exception.BussinesException`, `com.lorenzomar3.AQ.dto.newDto.IssueWhitItemsDTO` — mismos contratos, sin tocar su forma (solo cambia el tipo genérico de `itemList`, ver abajo).
- `TemarioRepositoryPort.findDirectChildren` / `AResponderChildRef` (spec 08) — sin cambios; el nuevo método es independiente.

### `content/application/port/out/` (nuevo)

- **`AResponderItemDetail`** — `record AResponderItemDetail(Long id, TipoAResponder tipo, String titulo, LocalDateTime fechaDeCreacion, Boolean esCritico, Integer numeroDePreguntas)`. Interno a `content.application`, sin anotaciones.

### `content/application/port/out/TemarioRepositoryPort` (modificado)

- Se agrega `List<AResponderItemDetail> findIssueItems(Long id)` — devuelve la raíz + todo lo que hoy trae `getIssueItems` (mismo alcance de 2 niveles).

### `content/infrastructure/persistence/adapter/TemarioJpaAdapter` (modificado)

- Implementa `findIssueItems` delegando en `aResponderRepositoryViejo.getIssueItems(id)`, mapeando cada `QuestionnaireItem` a `AResponderItemDetail` (`id`, `type`→`tipo`, `name`→`titulo`, `creationDate`→`fechaDeCreacion`, `isCritic`→`esCritico`, `numberOfQuestions`→`numeroDePreguntas`).

### `content/application/port/in/` y `content/application/service/` (nuevo)

- **`ObtenerItemsDeIssueUseCase`** — `IssueWhitItemsDTO obtenerItems(Long id)`.
- **`ObtenerItemsDeIssueService`** — implementa el puerto; inyecta `TemarioRepositoryPort`. Resuelve la raíz vía `findById` (`BussinesException` si no existe), obtiene la lista vía `findIssueItems(id)`, extrae la fila raíz (`esCritico`) y arma el resto como `itemList`, mapeando cada `AResponderItemDetail` restante a `IssueItemDTO`.

### `dto/newDto/` (nuevo)

- **`IssueItemDTO`** — `record IssueItemDTO(Long id, String name, TipoAResponder type, LocalDateTime creationDate, Boolean isCritic, Integer numberOfQuestions)`. Reemplaza a `QuestionnaireItem` como tipo de los elementos de `itemList` en la respuesta de este endpoint — mismo shape JSON (Jackson serializa records por nombre de componente, igual que ya hace `AResponderItemListDTO`).
- **`IssueWhitItemsDTO`** (existente, modificado solo en la firma del genérico) — `itemList` pasa de `List<QuestionnaireItem>` a `List<IssueItemDTO>`. El resto de campos (`id`, `name`, `creationDate`, `fatherid`, `type`, `isCritic`) no cambia.

### Sin cambios de esquema

Ninguna tabla ni columna nueva.

---

## Plan de implementación

1. **Record interno.** Crear `AResponderItemDetail(Long id, TipoAResponder tipo, String titulo, LocalDateTime fechaDeCreacion, Boolean esCritico, Integer numeroDePreguntas)` en `content.application.port.out`.
2. **Extender el puerto.** Agregar `List<AResponderItemDetail> findIssueItems(Long id)` a `TemarioRepositoryPort`.
3. **Implementar en el adapter.** En `TemarioJpaAdapter`, implementar `findIssueItems` delegando en `aResponderRepositoryViejo.getIssueItems(id)` (ya inyectado desde spec 08) y mapeando cada `QuestionnaireItem` a `AResponderItemDetail`.
4. **DTO de item nuevo.** Crear `IssueItemDTO(Long id, String name, TipoAResponder type, LocalDateTime creationDate, Boolean isCritic, Integer numberOfQuestions)` en `dto/newDto/`. Cambiar el tipo de `IssueWhitItemsDTO.itemList` de `List<QuestionnaireItem>` a `List<IssueItemDTO>`.
5. **Puerto in.** Crear `ObtenerItemsDeIssueUseCase` (`content.application.port.in`) con `IssueWhitItemsDTO obtenerItems(Long id)`.
6. **Servicio.** Implementar `ObtenerItemsDeIssueService`: resuelve la raíz vía `temarioRepositoryPort.findById(id)` (`BussinesException` si no existe); obtiene la lista completa vía `findIssueItems(id)`; extrae la fila raíz con `.filter(item -> item.id().equals(id)).findFirst().get()` (sin try/catch — cualquier excepción se propaga); arma `itemList` con el resto (`.filter(item -> !item.id().equals(id))`), mapeando cada `AResponderItemDetail` a `IssueItemDTO`; construye y devuelve el `IssueWhitItemsDTO`.
7. **Test de paridad (camino feliz).** Árbol con hijos directos mixtos (preguntas de varios tipos + subtemas) comparando `ObtenerItemsDeIssueService.obtenerItems` contra `PreguntaService.getIssueItems` (viejo): mismos valores en raíz (`id`, `name`, `creationDate`, `fatherid`, `type`, `isCritic`) e `itemList` (mismos ids, `name`, `type`, `creationDate`, `isCritic`, `numberOfQuestions`, sin importar orden).
8. **Test de not-found.** Id inexistente → `BussinesException`.
9. **Test de propagación de excepción.** Test unitario con `TemarioRepositoryPort` mockeado: `findById` devuelve un `Temario` válido pero `findIssueItems` devuelve una lista sin la fila cuyo id coincide con el pedido → se propaga `NoSuchElementException` (no se envuelve, no se devuelve lista vacía).
10. **Cablear `TemarioController`.** Reemplazar `preguntaService.getIssueItems(id)` por `obtenerItemsDeIssueUseCase.obtenerItems(id)` en `GET /issues/{id}/items` (`getTopicContent`).
11. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: abrir un tema con subtemas e hijos mixtos y confirmar que la vista de edición de issue (`issue-create-edit`) muestra los items, contadores y marcas de "crítico" igual que antes; confirmar que un id inexistente da un error visible; confirmar que el resto del CRUD (`POST`/`PUT`/`DELETE /issues`, `POST`/`PUT`/`DELETE /questions`, `question-ids`, `inverse`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [x] Existe `AResponderItemDetail(Long id, TipoAResponder tipo, String titulo, LocalDateTime fechaDeCreacion, Boolean esCritico, Integer numeroDePreguntas)` en `content.application.port.out`, sin ninguna anotación de Spring/JPA/Jackson.
- [x] `TemarioRepositoryPort` expone `findIssueItems(Long id)`; `TemarioJpaAdapter` lo implementa reutilizando `AResponderRepository.getIssueItems` (viejo), sin duplicar la query nativa.
- [x] Existe `IssueItemDTO` en `dto/newDto/`; `IssueWhitItemsDTO.itemList` es `List<IssueItemDTO>`.
- [x] Existe `ObtenerItemsDeIssueUseCase`/`ObtenerItemsDeIssueService`, que resuelve la raíz vía `TemarioRepositoryPort.findById` y lanza `BussinesException` si no existe.
- [x] `GET /issues/{id}/items` devuelve los mismos valores (raíz e `itemList`) que el camino viejo para un árbol con hijos directos mixtos (preguntas de varios tipos + subtemas), sin importar el orden de `itemList`.
- [x] `GET /issues/{id}/items` con un id inexistente lanza `BussinesException`.
- [x] `ObtenerItemsDeIssueService` **no** atrapa excepciones al extraer la fila raíz de `findIssueItems`: si la raíz no aparece en la lista, la excepción real (`NoSuchElementException`) se propaga sin envolver ni devolver `itemList` vacía.
- [x] `TemarioController.getTopicContent` delega en `ObtenerItemsDeIssueUseCase`, no en `preguntaService.getIssueItems`.
- [x] El resto de endpoints (`POST`/`PUT`/`DELETE /issues`, `POST`/`PUT`/`DELETE /questions`, `question-ids`, `inverse`, `GET /questions/fetch(-full)`) sigue funcionando sin cambios.
- [x] Existe un test de paridad (camino feliz) contra `PreguntaService.getIssueItems`, un test de not-found, y un test unitario que fuerza y verifica la propagación de la excepción cuando la raíz no aparece en `findIssueItems`.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: abrir un tema con subtemas e hijos mixtos y confirmar que `issue-create-edit` muestra items, contadores y marcas de "crítico" igual que antes; confirmar error visible ante un id inexistente. **(verificado por el usuario)**
- [x] `PreguntaService.getIssueItems` (viejo) no fue eliminado; queda sin uso desde el controller, como baseline del test de paridad.

---

## Decisiones tomadas y descartadas

- **Se agrega un método nuevo al puerto (`findIssueItems`/`AResponderItemDetail`) en vez de reutilizar `findDirectChildren`/`AResponderChildRef` (spec 08).**
  Confirmado con el usuario (siguiendo la recomendación). Descartado: extender `AResponderChildRef` con los campos faltantes, lo que hubiera obligado a tocar `ObtenerIdsDePreguntasService` y `CrearIssueInversoService` (spec 08), que ya dependen de su forma actual (solo `id`+`tipo`). Justificación: mantiene esos dos servicios intactos y aísla el riesgo a este spec.

- **Se reemplaza el manejo de errores "fail-silently" del código viejo por propagación directa de excepciones, sin `BussinesException` explícito para el caso de la raíz ausente en `findIssueItems`.**
  Confirmado con el usuario. Descartado: (a) mantener el `catch` genérico que hoy traga cualquier error y devuelve `itemList` vacía; (b) envolver el caso de raíz ausente en un `BussinesException` propio. Justificación: el escenario de raíz ausente es puramente defensivo (no debería ocurrir en la práctica si el id existe), y el usuario prefirió simplicidad — dejar que la excepción real se propague — antes que agregar un tipo de error nuevo para un caso que nunca se ejercita con datos reales.

- **Se preserva la limitación de "solo 2 niveles de profundidad" en `isCritic`/`numberOfQuestions`, heredada de la query nativa (`//Analizar.` en el código viejo).**
  Confirmado con el usuario. Mismo criterio de no cambiar comportamiento de negocio durante una migración técnica, usado en specs 07/08.

- **Se preservan las inconsistencias preexistentes del contrato de wire** (`type` string en la raíz vs. enum en cada item; `fatherid` que el frontend no declara; `lastUpdateDate` que el frontend espera pero el backend nunca envía).
  Confirmado con el usuario. Ningún spec anterior tocó `AQ-SIMPLE-FRONT`; corregir esto ahora sería un cambio de contrato fuera del alcance de una migración de arquitectura.

- **La raíz se resuelve vía `TemarioRepositoryPort.findById` (nuevo camino, spec 07) en vez de reutilizar `TemarioRepository.findByIdBasic`/`IssueOrQuestionnaireProjection` (viejo).**
  Justificación: `content.domain.Temario` ya expone exactamente los campos que necesita la raíz (`titulo`, `fechaDeCreacion`, `idDuenio`, `tipo`), evitando depender de una projection vieja adicional cuando el puerto nuevo ya resuelve lo mismo.

- **Se crea `IssueItemDTO` nuevo en vez de reutilizar la projection JPA `QuestionnaireItem` como tipo de `itemList`.**
  Justificación: `QuestionnaireItem` es una interfaz de projection de Spring Data que no puede instanciarse a mano; la capa de aplicación no puede construirla directamente. Mismo motivo por el que spec 08 introdujo `AResponderChildRef` en vez de reutilizar `QuestionnaireItem` en `findDirectChildren`.

- **`IssueWhitItemsDTO` (existente) se modifica solo en el tipo genérico de `itemList`**, en vez de crear un DTO de respuesta completamente nuevo.
  Justificación: el resto de sus campos no cambia; crear un DTO nuevo hubiera sido una abstracción redundante para un cambio que solo afecta un campo.

- **`PreguntaService.getIssueItems` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores.

---

## Riesgos identificados

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas**, en un camino de lectura con dos fuentes distintas: la raíz se lee vía `TemarioJpaRepository` (nuevo, a través de `findById`) mientras `itemList` se lee vía `AResponderRepository` (viejo, a través de `findIssueItems`). Mismo riesgo ya identificado en specs 01-09, ahora combinando ambas fuentes dentro de la misma respuesta.
  *Mitigación:* el test de paridad corre contra estado limpio, sin operar simultáneamente sobre la misma fila desde ambos caminos en la misma transacción.

- **Cambio de comportamiento observable ante el escenario de raíz ausente en `findIssueItems`.** Antes, ese caso (extremadamente improbable) devolvía 200 con `itemList` vacía; ahora puede propagar una excepción no controlada (`NoSuchElementException`) que, sin `@ControllerAdvice`, se traduce en un 500 genérico para el usuario final de `AQ-SIMPLE-FRONT`.
  *Mitigación:* aceptado explícitamente por el usuario — el escenario nunca ocurre con datos consistentes; se prioriza fallar de forma visible sobre enmascarar un error real. El test unitario del paso 9 documenta y fija este comportamiento.

- **Condición de carrera (TOCTOU) entre `findById` (raíz) y `findIssueItems` (resto).** Si otra request borra el `Temario` entre ambas llamadas, `findIssueItems` podría devolver una lista vacía o inconsistente, disparando el mismo `NoSuchElementException` del punto anterior por una causa distinta (borrado concurrente, no dato corrupto).
  *Mitigación:* ninguna en este spec — mismo nivel de garantía de concurrencia que el resto de la aplicación (sin locking), consistente con el riesgo ya aceptado en spec 09.

- **`getIssueItems` (query nativa) podría no cubrir correctamente un tipo de pregunta nuevo** si en el futuro se agrega uno sin actualizar su `UNION`, haciendo que `findIssueItems` omita silenciosamente hijos de ese tipo — mismo riesgo ya documentado en spec 08 para `findDirectChildren`, ahora también presente en `findIssueItems` porque comparten la query subyacente.
  *Mitigación:* el test de paridad del paso 7 cubre una mezcla de tipos de pregunta ya migrados; un tipo nuevo requeriría extender el test, mismo criterio documentado en `CLAUDE.md`.

- **`IssueWhitItemsDTO.itemList` cambia de tipo (`QuestionnaireItem` → `IssueItemDTO`)**, lo cual es un cambio de tipo Java aunque no de contrato JSON. Si algún otro punto del código viejo (no detectado en la investigación) también construyera o leyera `IssueWhitItemsDTO` esperando `QuestionnaireItem`, ese código dejaría de compilar y quedaría expuesto en tiempo de build, no en runtime.
  *Mitigación:* ninguna adicional necesaria — un error de compilación es preferible a uno silencioso, y `./mvnw test`/`package` (paso 11) lo detectaría de inmediato si existiera.

---

## Próximo spec sugerido

Con este spec, `TemarioController` queda 100% migrado (8/8 endpoints). En `PreguntaController` siguen sin migrar `POST /questions/fetch`, `POST /questions/fetch-full`, `POST /questions/verify` y `POST /questions/inverse` (individual); `ResponderController` sigue completo en código viejo (`random-ids`, `critical-ids`).

**Recomendación: migrar `POST /questions/fetch` y `POST /questions/fetch-full` a continuación.** Es la migración más parecida a la de este spec — lectura pura, sin lógica de negocio riesgosa, reutilizando el dispatch por tipo ya existente desde specs 01-06 — y desbloquea las pantallas `question-view`/`question-response` del frontend. Se descarta arrancar por `POST /questions/verify` porque toca la lógica de "crítico" (mutación de `intentosParaQueDejeDeSerCriticoDisponible`), más delicada y candidata a su propio spec dedicado en vez de combinarla con otra migración.
