# Spec 08 — Migración hexagonal: recorrido de árbol (question-ids) e inversión de preguntas (issues/inverse)

**Estado:** Implemented
**Dependencias:** Spec 01 (hexagonal-content-piloto) — reutiliza `content.domain.PreguntaSimple`, `PreguntaSimpleRepositoryPort`, `PreguntaSimpleJpaAdapter`, `PreguntaSimpleMapper`; Spec 07 (hexagonal-issue-crud-basico) — extiende `TemarioRepositoryPort`/`TemarioJpaAdapter`, reutiliza el patrón de inyectar el repositorio JPA viejo dentro de un adapter nuevo (usado ahí para el borrado en cascada, aquí para leer hijos directos vía la query nativa `UNION` de `AResponderRepository.getIssueItems`).
**Fecha:** 2026-08-01
**Objetivo:** Migrar `GET /issues/{id}/question-ids` (recorrido recursivo del árbol de un `Temario` para recolectar los ids de toda pregunta hoja) y `POST /issues/inverse` (crear un `Temario` hermano con sus `PreguntaSimple` hijas directas invertidas) a arquitectura hexagonal, agregando a `TemarioRepositoryPort` un método de lectura de hijos directos que reutiliza la query nativa vieja en vez de reconstruirla.

---

## Alcance

### Incluido

- **Infraestructura de persistencia:**
  - `TemarioRepositoryPort` — se agrega `List<AResponderChildRef> findDirectChildren(Long id)`, donde `AResponderChildRef` es un record nuevo y puramente interno a `content.application` (`id: Long`, `tipo: TipoAResponder`) — no es una entidad de dominio, no se expone en ningún DTO de wire.
  - `TemarioJpaAdapter` — implementa `findDirectChildren` inyectando el `AResponderRepository` viejo y delegando en su query nativa existente (`getIssueItems`, la misma que usa hoy `GET /issues/{id}/items`), mapeando cada `QuestionnaireItem` a `AResponderChildRef(id, type)`. Mismo patrón que spec 07 usó para `deleteById` (reusar repositorio JPA viejo dentro de un adapter nuevo).
- **Application — un puerto in y un servicio por endpoint:**
  - `ObtenerIdsDePreguntasUseCase` / `ObtenerIdsDePreguntasService` (`GET /issues/{id}/question-ids`): valida que el `Temario` raíz existe (`TemarioRepositoryPort.findById`, `BussinesException` si no); recorre recursivamente vía `findDirectChildren` — si el hijo es `CUESTIONARIO`/`TEMA`/`SUBTEMA` recursa, si es cualquiera de los 6 tipos de pregunta agrega su id al resultado. Sin filtrar por tipo de pregunta (paridad: recoge toda hoja `Pregunta`, a cualquier profundidad).
  - `CrearIssueInversoUseCase` / `CrearIssueInversoService` (`POST /issues/inverse`): busca el `Temario` por id (`BussinesException` si no existe); obtiene sus hijos directos vía `findDirectChildren` y filtra solo `PREGUNTA_SIMPLE` (no recursivo, ignora silenciosamente subtemas y otros tipos — paridad exacta); por cada id filtrado busca el `PreguntaSimple` completo vía `PreguntaSimpleRepositoryPort.findById`; invierte cada uno con lógica plana en el propio service (intercambia `titulo`↔`respuestaEstablecida`, limpia HTML de la respuesta original con `Jsoup.parse(...).text()` antes de usarla como nuevo `titulo`, `respuestaPrecisa=false`, `intentosParaQueDejeDeSerCriticoDisponible=0`, `fechaDeCreacion`/`ultimaActualizacion=now()` — mismos defaults que produce hoy el modelo viejo); crea el nuevo `Temario` (mismo `idDuenio` y `tipo` que el original, `titulo` del DTO) y lo persiste vía `TemarioRepositoryPort.save`; luego persiste cada pregunta invertida con `idDuenio` = id del nuevo `Temario`, vía `PreguntaSimpleRepositoryPort.save` (mismo criterio de "fila independiente" de specs 02-07). Devuelve el `AResponderItemListDTO` del nuevo `Temario`.
- **Dispatch en `TemarioController`:** `GET /issues/{id}/question-ids` y `POST /issues/inverse` delegan a los nuevos use cases en vez de a `temarioService`.
- **Tests:** tests de paridad comparando contra `TemarioService.obtenerTodosLosIdsDePreguntas` y `crearTemarioPreguntasInversa` (viejos): árbol con subtemas anidados y mezcla de tipos de pregunta para `question-ids` (confirmando que recoge todas las hojas a profundidad >1, sin filtrar por tipo); para `inverse`, un `Temario` con hijos directos mixtos (`PREGUNTA_SIMPLE` + otros tipos/subtemas) verificando que solo se invierten los `PREGUNTA_SIMPLE` directos, que el contenido invertido es correcto (títulos/respuestas intercambiados, HTML limpiado), y que el nuevo `Temario` queda como hermano del original (mismo `idDuenio`, mismo `tipo`).

### Explícitamente NO incluido

- `GET /issues/{id}/items` — sigue 100% en código viejo (`PreguntaService.getIssueItems`); este spec solo reutiliza su query nativa subyacente desde el adapter nuevo, no migra el endpoint en sí.
- Eliminar `TemarioService`, `PreguntaService.getListOfPreguntaSimples` ni ningún otro código muerto — diferido a limpieza final, mismo criterio que spec 07.
- Cualquier cambio de esquema de base de datos.
- Mover `TemarioController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Cambiar el comportamiento de negocio de `issues/inverse` (hacerlo recursivo, o dejar de ignorar silenciosamente subtemas/otros tipos) — paridad exacta, confirmado.
- Agregar métodos `findByIdDuenio`/similares a los puertos de los otros 6 tipos de pregunta — no hace falta, `findDirectChildren` resuelve la navegación delegando en la query vieja, no en los puertos nuevos por tipo.
- Optimizar el patrón N+1 de la recursión (una query por nodo visitado) — no es peor que el comportamiento actual (carga del árbol completo vía JPA), y no se aborda performance en este spec.
- El slice `answering/` (`verifyResponse`, lógica de críticos) — no tocado.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `content.domain.PreguntaSimple`, `PreguntaSimpleRepositoryPort`, `PreguntaSimpleJpaAdapter`, `PreguntaSimpleMapper` (spec 01) — se reutilizan sin ningún cambio, tanto para leer la pregunta original como para persistir la invertida.
- `content.domain.Temario`, `content.domain.AResponder` — sin cambios estructurales; siguen anémicos (sin `listaAResponder`), consistente con spec 07.
- `com.lorenzomar3.AQ.model.TipoAResponder`, `InverseIssueCreateDTO(Long idIssue, String name)`, `AResponderItemListDTO(Long id, String name, TipoAResponder type, Integer numberOfQuestions, Boolean isCritic)` — mismos contratos de wire, sin tocar.
- `AResponderRepository.getIssueItems(Long id)` y la projection `QuestionnaireItem` (código viejo, `projections/`) — reutilizados tal cual desde `TemarioJpaAdapter`, sin modificarlos.

### `content/application/` (nuevo)

- **`AResponderChildRef`** — record nuevo en `content.application.port.out` (mismo paquete que `TemarioRepositoryPort`): `id: Long`, `tipo: TipoAResponder`. Puramente interno, usado solo como tipo de retorno de `findDirectChildren`; no se serializa ni se expone en ningún DTO de wire.
- **Puertos in:** `ObtenerIdsDePreguntasUseCase`, `CrearIssueInversoUseCase`.
- **Servicios:** `ObtenerIdsDePreguntasService`, `CrearIssueInversoService`.

### `content/infrastructure/persistence/` (modificado)

- **`TemarioRepositoryPort`** — se agrega `List<AResponderChildRef> findDirectChildren(Long id)`.
- **`TemarioJpaAdapter`** — implementa `findDirectChildren` inyectando adicionalmente `AResponderRepository` (viejo, junto al `Repository.TemarioRepository` ya inyectado desde spec 07) y delegando en `getIssueItems(id)`, mapeando cada `QuestionnaireItem` a `AResponderChildRef`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutilizan `aresponder`, `temario` y las tablas de los 6 tipos de pregunta sin modificarlas.

---

## Plan de implementación

1. **Tipo interno.** Crear el record `AResponderChildRef(Long id, TipoAResponder tipo)` en `content.application.port.out`.
2. **Puerto.** Extender `TemarioRepositoryPort` con `List<AResponderChildRef> findDirectChildren(Long id)`.
3. **Adapter.** Implementar `findDirectChildren` en `TemarioJpaAdapter`, inyectando `AResponderRepository` (viejo) además del `Repository.TemarioRepository` ya inyectado desde spec 07; delega en `getIssueItems(id)` y mapea cada `QuestionnaireItem` a `AResponderChildRef`.
4. **`question-ids`.** Implementar `ObtenerIdsDePreguntasUseCase`/`Service`: valida que el `Temario` raíz existe (`findById`, `BussinesException` si no), recorre recursivamente vía `findDirectChildren` (recursa en `CUESTIONARIO`/`TEMA`/`SUBTEMA`, acumula ids de cualquier tipo de pregunta).
5. **`issues/inverse`.** Implementar `CrearIssueInversoUseCase`/`Service`: busca el `Temario` por id, filtra hijos directos `PREGUNTA_SIMPLE` vía `findDirectChildren`, carga cada uno completo (`PreguntaSimpleRepositoryPort.findById`), invierte con lógica plana en el service (Jsoup + defaults acordados), crea y persiste el nuevo `Temario` hermano, persiste cada pregunta invertida con `idDuenio` del nuevo `Temario`.
6. **Test de paridad.** Crear el/los test(s) de integración comparando contra `TemarioService.obtenerTodosLosIdsDePreguntas`/`crearTemarioPreguntasInversa` (viejos): árbol con subtemas anidados y mezcla de tipos de pregunta para `question-ids`; hijos directos mixtos, contenido invertido y relación de hermano para `inverse`.
7. **Cablear `TemarioController`.** Reemplazar las llamadas a `temarioService` por los nuevos use cases en `GET /issues/{id}/question-ids` y `POST /issues/inverse`.
8. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: pedir los ids de preguntas de un tema con subtemas anidados, crear un issue invertido a partir de un tema con preguntas simples (y confirmar que el resultado aparece como hermano, con las preguntas invertidas correctamente), y confirmar que el resto de endpoints (CRUD de los 6 tipos de pregunta, `GET /issues/{id}/items`) siguen funcionando sin cambios.

---

## Criterios de aceptación

- [ ] Existe `AResponderChildRef(Long id, TipoAResponder tipo)` en `content.application.port.out`, sin ninguna anotación de Spring/JPA/Jackson.
- [ ] `TemarioRepositoryPort` expone `findDirectChildren(Long id)`; `TemarioJpaAdapter` lo implementa reutilizando `AResponderRepository.getIssueItems` (viejo), sin duplicar la query nativa `UNION`.
- [ ] `GET /issues/{id}/question-ids` devuelve la misma lista de ids (sin importar orden) que el camino viejo, incluyendo casos con subtemas anidados a profundidad > 1 y con mezcla de los 6 tipos de pregunta.
- [ ] `GET /issues/{id}/question-ids` lanza `BussinesException` si el id no corresponde a un `Temario` existente.
- [ ] `POST /issues/inverse` crea un `Temario` nuevo con mismo `idDuenio` y `tipo` que el original (hermano, no hijo) y `titulo` tomado del DTO.
- [ ] `POST /issues/inverse` solo invierte los hijos directos de tipo `PREGUNTA_SIMPLE`, ignorando silenciosamente subtemas y cualquier otro tipo de pregunta, sin recursar.
- [ ] Cada pregunta invertida tiene `titulo`/`respuestaEstablecida` intercambiados (con el HTML de la respuesta original limpiado vía Jsoup), `respuestaPrecisa=false`, `intentosParaQueDejeDeSerCriticoDisponible=0`, y queda persistida con `idDuenio` apuntando al nuevo `Temario`.
- [ ] `POST /issues/inverse` lanza `BussinesException` si el id no corresponde a un `Temario` existente.
- [ ] La respuesta de `POST /issues/inverse` es el `AResponderItemListDTO` del `Temario` recién creado.
- [ ] `GET /issues/{id}/items` y el CRUD de los 6 tipos de pregunta ya migrados siguen funcionando sin cambios.
- [ ] Existe un test de paridad que compara `question-ids` contra `TemarioService.obtenerTodosLosIdsDePreguntas` (árbol con subtemas anidados y tipos mixtos) e `issues/inverse` contra `TemarioService.crearTemarioPreguntasInversa` (hijos directos mixtos, contenido invertido, relación de hermano).
- [ ] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT`: pedir ids de preguntas de un tema con subtemas y crear un issue invertido funcionan sin errores visibles. **(verificado por el usuario)**
- [ ] `TemarioService` (viejo) no fue eliminado; sus métodos migrados quedan como baseline de los tests de paridad.

---

## Decisiones tomadas y descartadas

- **Un solo spec para ambos endpoints**, pese a que la recomendación inicial era separarlos en dos por tener formas distintas (lectura recursiva vs. escritura con inversión).
  Confirmado con el usuario. Justificación: ambos comparten la misma pieza de infraestructura nueva (`findDirectChildren`/`AResponderChildRef`), y juntos cierran por completo la migración del CRUD de `Temario`/`Issue` iniciada en spec 07.

- **La recursión de `question-ids` se reimplementa en la capa de aplicación** (llamando repetidamente a `findDirectChildren` por cada hijo contenedor), en vez de agregar `listaAResponder` a `content.domain.Temario`.
  Confirmado con el usuario. Justificación: mantiene el domain nuevo anémico (mismo criterio de todos los specs anteriores) sin tener que modelar la relación bidireccional solo para una lectura de ids.

- **`findDirectChildren` reutiliza la query nativa vieja (`AResponderRepository.getIssueItems`)**, en vez de construir queries nuevas por tipo.
  Confirmado con el usuario. Descartado: agregar un método `findByIdDuenio` a los 7 puertos existentes (`TemarioRepositoryPort` + los 6 puertos de tipo de pregunta) para reconstruir la navegación de forma "pura". Justificación: la query vieja ya resuelve correctamente el `UNION` sobre las 7 tablas; reimplementarla sería trabajo redundante fuera de la escala de este spec, mismo criterio que spec 07 usó para el borrado en cascada.

- **`issues/inverse` mantiene paridad exacta de comportamiento**: solo mira hijos directos `PREGUNTA_SIMPLE`, no es recursivo, e ignora silenciosamente subtemas y otros tipos.
  Confirmado con el usuario. Descartado: aprovechar la migración para cambiar ese comportamiento (hacerlo recursivo o advertir sobre tipos ignorados). Justificación: es una decisión de negocio fuera del alcance de una migración técnica.

- **`inversar()` se reimplementa como lógica plana dentro de `CrearIssueInversoService`**, no como método de `content.domain.PreguntaSimple`.
  Confirmado con el usuario. Mismo criterio que `CrearIssueService` (spec 07) con la asignación de tipo: el domain nuevo se mantiene 100% anémico en todos los specs anteriores.

- **La pregunta invertida usa los mismos defaults que produce hoy el modelo viejo** (`respuestaPrecisa=false`, `intentosParaQueDejeDeSerCriticoDisponible=0`, `fechaDeCreacion`/`ultimaActualizacion=now()`), aunque `inversar()` no los setee explícitamente en el código viejo.
  Confirmado con el usuario. Justificación: son los valores que produce el inicializador de campo de la entity vieja al construir `new PreguntaSimple(...)` — paridad exacta, no un valor arbitrario nuevo.

- **`AResponderChildRef` vive en `content.application.port.out`, no en `content.domain`.**
  Justificación: es un tipo de transporte puramente interno para resolver la navegación del árbol, no un concepto de negocio real — no tiene sentido modelarlo como entidad de dominio.

- **No se optimiza el patrón N+1 de la recursión** (una query por nodo visitado).
  Descartado: no es peor que el comportamiento actual (carga del árbol completo vía JPA en una sola consulta, pero con el mismo costo de N accesos a filas); optimizar performance queda fuera del alcance de esta migración arquitectónica.

- **`GET /issues/{id}/items` no se migra en este spec**, aunque se reutiliza su query subyacente.
  Descartado: ampliar el alcance para migrar también ese endpoint. Justificación: mismo criterio de "un problema a la vez" — este spec solo necesita los hijos directos como insumo interno, no expone ese endpoint como tal.

---

## Riesgos identificados

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas**, ahora también en un camino de **lectura** compartido: `TemarioJpaAdapter.findDirectChildren` delega en `AResponderRepository` (viejo) mientras el resto del adapter usa `TemarioJpaRepository` (nuevo). Mismo riesgo ya identificado en specs 01-07, extendido a una query que ambos caminos podrían ejecutar dentro de la misma transacción.
  *Mitigación:* el test de paridad corre contra estado limpio, sin operar simultáneamente sobre la misma fila desde ambos caminos.

- **La query nativa `getIssueItems` podría no cubrir correctamente los 6 tipos de pregunta** si en el futuro se agrega un tipo nuevo sin actualizar su `UNION`. Un desajuste ahí haría que `question-ids`/`inverse` omitan silenciosamente hijos de ese tipo, sin lanzar ningún error.
  *Mitigación:* el test de paridad del paso 6 incluye un árbol con mezcla de los 6 tipos de pregunta ya migrados, verificando que ninguno se pierda.

- **Recursión con una query por nodo visitado (N+1)** puede ser lenta en árboles muy profundos o anchos. Aceptado explícitamente como fuera de alcance (ver Decisiones), pero vale la pena tenerlo presente si en producción aparecen cuestionarios con estructuras grandes.
  *Mitigación:* ninguna en este spec; si se vuelve un problema real, amerita su propio spec de optimización.

- **El filtro "solo `PREGUNTA_SIMPLE`, hijos directos, no recursivo" de `issues/inverse` es fácil de romper accidentalmente** al reimplementarlo (p. ej. olvidar el filtro de tipo, o recursar por error reusando la misma función que `question-ids`).
  *Mitigación:* el test de paridad del paso 6 usa explícitamente un `Temario` con hijos directos mixtos (`PREGUNTA_SIMPLE` + subtemas + otros tipos) para confirmar que solo se invierten los correctos.

- **Los defaults asumidos para la pregunta invertida** (`respuestaPrecisa=false`, `intentosParaQueDejeDeSerCriticoDisponible=0`, fechas `now()`) dependen de inicializadores de campo del modelo **viejo**, no de una regla de negocio documentada. Si esos defaults cambiaran en el modelo viejo sin que alguien revise este spec, el camino nuevo quedaría desincronizado en silencio.
  *Mitigación:* ninguna automática — queda documentado aquí como el origen de la paridad, para que una futura limpieza del modelo viejo lo tenga en cuenta.

- **Mapeo incorrecto de `QuestionnaireItem` → `AResponderChildRef`** (en particular el campo `tipo`) podría hacer que la recursión de `question-ids` corte antes de tiempo (tratando un contenedor como hoja) o recurse sobre una pregunta por error.
  *Mitigación:* el test de paridad del paso 6 incluye un árbol con profundidad > 1 (subtema dentro de tema), que solo pasa si la distinción contenedor/hoja es correcta en cada nivel.
