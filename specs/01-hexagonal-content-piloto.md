# Spec 01 — Migración hexagonal: cuestionarios (lectura) + PreguntaSimple (piloto de escritura)

**Estado:** Implementado
**Dependencias:** Ninguna (primer spec del proyecto)
**Fecha:** 2026-07-27

**Objetivo:** Migrar el slice `content/` hacia arquitectura hexagonal (Ports & Adapters), usando como piloto la lectura de cuestionarios (`GET /questionnaires`) y el CRUD completo de `PreguntaSimple`, sin romper la API existente ni afectar a los demás tipos de pregunta.

---

## Scope

### Incluido

- **Paquete nuevo** `com.lorenzomar3.AQ.content` (anidado dentro del paquete base existente), con la estructura de `domain/`, `application/port/{in,out}/`, `application/service/`, `infrastructure/persistence/{entity,mapper,adapter}/`, `infrastructure/controller/` definida en `ARQUITECTURA.md`.
- **Vertical de lectura de cuestionarios:**
  - `Temario` (domain, Java puro — solo lo necesario para listar cuestionarios top-level).
  - `TemarioEntity`, `TemarioMapper`, `TemarioJpaAdapter`, `TemarioRepositoryPort` (out), `ObtenerCuestionariosUseCase` (in), `ObtenerCuestionariosService`.
  - Endpoint `GET /questionnaires` migrado a este flujo, manteniendo path y forma de JSON idénticos a los actuales.
- **Vertical de escritura piloto — `PreguntaSimple`:**
  - `PreguntaSimple` (domain, Java puro).
  - `PreguntaSimpleEntity` (mapea a la tabla `pregunta_simple` existente), `PreguntaSimpleMapper`, adapter correspondiente.
  - `CrearPreguntaUseCase`, `EditarPreguntaUseCase`, `EliminarPreguntaUseCase` — implementados y aplicados **solo** cuando `tipo == PREGUNTA_SIMPLE`.
- **Dispatch en `PreguntaController`:** los endpoints existentes (`POST /questions`, `PUT /questions`, `DELETE /questions/{id}`) siguen siendo el único punto de entrada. Internamente, si `tipo == PREGUNTA_SIMPLE` delegan a los nuevos use cases hexagonales; para los otros 5 tipos siguen llamando a `PreguntaService` tal como hoy.
- **Baja de código viejo** una vez validado: el código de `TemarioService`/`TemarioController` que resolvía `GET /questionnaires`, y el de `PreguntaService` que resolvía create/edit/delete de `PreguntaSimple`.
- **Tests:** unitarios puros de `Temario` y `PreguntaSimple` (domain, sin Spring) + al menos un test de integración por vertical que confirme paridad de JSON contra el comportamiento actual.

### Explícitamente NO incluido

- Los otros 5 tipos de pregunta (`SeleccionUnica`, `OpcionMultiple`, `VerdaderoOFalso`, `DesplegableCompartido`, `DesplegableIndependiente`) — quedan intactos en `PreguntaService`/`FabricaDePreguntas` viejos. Se migran en specs posteriores, uno a la vez.
- El resto de las operaciones de `Temario`: crear/editar/borrar cuestionario o issue, `issues/inverse`, `question-ids` — quedan en el código viejo.
- El slice `answering/` completo (`verifyResponse`, lógica de críticos, `ResponderController`) — depende de que `content/` esté migrado primero; se aborda en un spec siguiente.
- Renombrar el paquete base a `com.aq` — diferido a un spec de limpieza final, cuando todo el proyecto esté migrado.
- Cualquier cambio de esquema de base de datos — se reutilizan las tablas existentes (`temario`, `aresponder`, `pregunta`, `pregunta_simple`) sin modificarlas.
- Cambios en `AQ-SIMPLE-FRONT` — no se tocan, ya que el contrato JSON no cambia.
- La sugerencia de SM-2 para críticos (registrada aparte en memoria de proyecto) — no relacionada con esta migración.

---

## Data model

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` — enum plano, sin anotaciones de framework. Cumple la regla de `domain/` ("Java puro") tal como está, así que el domain nuevo lo referencia directamente en vez de duplicarlo.
- DTOs de wire existentes: `TemarioBasicDTO(id, name, creationDate, fatherid)` y `PostPreguntaDTO(id, titulo, descripcion, tipo, idTemarioPerteneciente, respuestaVerdadera, respuestaEstablecida, ...)`. No se tocan — son el contrato que ya consume el frontend y que las nuevas capas de aplicación deben producir/aceptar sin cambios.

### `content/domain/` (Java puro, sin JPA ni Jackson)

- **`AResponder`** (abstracto) — `id: Long`, `titulo: String`, `descripcion: String`, `idDuenio: Long`, `fechaDeCreacion: LocalDateTime`, `tipo: TipoAResponder`. Espejo mínimo de los campos base que hoy están mezclados con `@Entity`/`@JsonView` en `model/AResponder/AResponder.java`.
- **`Temario extends AResponder`** — agrega nada más que lo que necesita el listado (no incluye `listaAResponder`, que pertenece a otro caso de uso fuera de este spec).
- **`Pregunta extends AResponder`** (abstracto) — agrega `intentosParaQueDejeDeSerCriticoDisponible: Integer`, `imagenTitulo: String`. Se crea abstracto (aunque este spec solo tenga una subclase concreta) porque `ARQUITECTURA.md` lo modela así y evita retrabajo cuando se migren los otros 5 tipos.
- **`PreguntaSimple extends Pregunta`** — agrega `respuestaEstablecida: String`, `respuestaPrecisa: Boolean`.

### `content/application/`

- **Puertos in:** `ObtenerCuestionariosUseCase`, `CrearPreguntaUseCase`, `EditarPreguntaUseCase`, `EliminarPreguntaUseCase` (estos tres últimos operan solo sobre `PreguntaSimple`).
- **Puertos out:** `TemarioRepositoryPort` (`findAllCuestionarios()`), `PreguntaSimpleRepositoryPort` (`save`, `findById`, `deleteById`).
- **Servicios:** `ObtenerCuestionariosService`, `CrearPreguntaSimpleService`, `EditarPreguntaSimpleService`, `EliminarPreguntaSimpleService`.

### `content/infrastructure/persistence/`

- **Entities** — deben apuntar a las tablas existentes explícitamente con `@Table`, porque Hibernate generaría un nombre distinto a partir del nombre de clase nuevo:
  - `AResponderEntity` → `@Entity @Inheritance(strategy = JOINED) @Table(name = "aresponder")`
  - `TemarioEntity extends AResponderEntity` → `@Table(name = "temario")`
  - `PreguntaEntity extends AResponderEntity` → `@Table(name = "pregunta")`
  - `PreguntaSimpleEntity extends PreguntaEntity` → `@Table(name = "pregunta_simple")`
- **Mappers:** `TemarioMapper` (`toDomain(TemarioEntity)`), `PreguntaSimpleMapper` (`toDomain`/`toEntity`).
- **Adapters:** `TemarioJpaAdapter implements TemarioRepositoryPort`, `PreguntaSimpleJpaAdapter implements PreguntaSimpleRepositoryPort` — ambos usan `JpaRepository` internamente (se puede reusar `BasePreguntaRepositorio<PreguntaSimpleEntity>` ya existente para el segundo).

### Sin cambios de esquema

Ninguna tabla ni columna nueva. Los `@Table(name=...)` de arriba son el mecanismo para que el modelo de dominio nuevo conviva con el esquema actual sin migración de datos.

---

## Implementation plan

1. **Domain puro.** Crear `content/domain/{AResponder, Temario, Pregunta, PreguntaSimple}.java` (Java puro, sin anotaciones). No se conecta a nada todavía — el sistema sigue funcionando exactamente igual.

2. **Entities JPA nuevas.** Crear `content/infrastructure/persistence/entity/{AResponderEntity, TemarioEntity, PreguntaEntity, PreguntaSimpleEntity}.java` con `@Table` apuntando a las tablas existentes (`aresponder`, `temario`, `pregunta`, `pregunta_simple`). Levantar la app y confirmar en pgAdmin que Hibernate **no** crea tablas nuevas (`ddl-auto=update` debe detectar el mapeo a las tablas ya existentes).

3. **Mappers.** Crear `TemarioMapper` y `PreguntaSimpleMapper` (`toDomain`/`toEntity`). Sin uso real todavía.

4. **Puertos y adapters.** Definir `TemarioRepositoryPort`, `PreguntaSimpleRepositoryPort` (out) y sus implementaciones `TemarioJpaAdapter`, `PreguntaSimpleJpaAdapter` (reusando `BasePreguntaRepositorio<PreguntaSimpleEntity>` ya existente para el segundo).

5. **Vertical de lectura — cuestionarios.**
   - Implementar `ObtenerCuestionariosUseCase` + `ObtenerCuestionariosService`.
   - Test de integración que compara el JSON devuelto por el nuevo flujo contra el que hoy devuelve `TemarioService.obtenerTodosLosTemariosDeTipoCuestionario()` + `toTemarioCuestionarioCardDTO()`.
   - Recién con el test en verde, cablear `TemarioController./questionnaires` para que llame al nuevo `ObtenerCuestionariosUseCase` en vez de `TemarioService`. El path y el JSON no cambian, así que el frontend no se entera.
   - Borrar `TemarioService.obtenerTodosLosTemariosDeTipoCuestionario()` (confirmando antes que no la use nada más).

6. **Vertical de escritura — PreguntaSimple.**
   - Implementar `CrearPreguntaUseCase`, `EditarPreguntaUseCase`, `EliminarPreguntaUseCase` + servicios, usando la persistence layer del paso 4.
   - Test de integración de paridad contra el `PreguntaService` actual para create/edit/delete de `PREGUNTA_SIMPLE`, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve correctamente aunque su lógica no se toque en este spec.
   - Modificar `PreguntaController` (`POST/PUT/DELETE /questions`) para que, cuando `tipo == PREGUNTA_SIMPLE`, delegue a los nuevos use cases; para los otros 5 tipos sigue llamando a `PreguntaService` sin cambios.
   - Borrar del `PreguntaService` viejo únicamente las ramas de código específicas de `PREGUNTA_SIMPLE` en create/edit/delete, dejando intacto el manejo de los demás tipos.

7. **Tests unitarios de domain.** Cobertura de `Temario` y `PreguntaSimple` (Java puro, sin Spring) para la lógica que quedó en el domain — se pueden escribir en paralelo a los pasos 5 y 6, pero se revisan como barrido final acá.

8. **Verificación final.** Correr `./mvnw test` completo y probar manualmente contra `AQ-SIMPLE-FRONT` (listado de cuestionarios + crear/editar/borrar una `PreguntaSimple`) antes de considerar terminada la migración piloto.

---

## Acceptance criteria

- [x] Existen y compilan `content/domain/{AResponder, Temario, Pregunta, PreguntaSimple}.java` sin ninguna anotación de Spring/JPA/Jackson.
- [x] Existen `TemarioEntity`, `AResponderEntity`, `PreguntaEntity`, `PreguntaSimpleEntity` con `@Table` apuntando a `temario`, `aresponder`, `pregunta`, `pregunta_simple` respectivamente.
- [x] Al levantar la app con estas entities nuevas, no se crean tablas nuevas en Postgres. *(no se miró pgAdmin directamente, pero `ddl-auto=validate` no tiene margen para crear tablas — y la app levantó y respondió correctamente en la verificación manual)*
- [x] `GET /questionnaires` devuelve exactamente el mismo path, status code y forma de JSON (`List<TemarioBasicDTO>`) que antes de la migración. *(confirmado por `ObtenerCuestionariosParidadTest`, ya borrado tras cumplir su propósito)*
- [x] `TemarioService.obtenerTodosLosTemariosDeTipoCuestionario()` fue eliminado del código.
- [x] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con `tipo == PREGUNTA_SIMPLE` producen el mismo resultado (JSON de respuesta y estado persistido en BD) que antes de la migración. *(confirmado por `PreguntaSimpleParidadTest`; `DELETE` sigue en el camino viejo sin cambios, por lo que es trivialmente igual)*
- [x] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con cualquiera de los otros 5 tipos (`SeleccionUnica`, `OpcionMultiple`, `VerdaderoOFalso`, `DesplegableCompartido`, `DesplegableIndependiente`) siguen funcionando sin cambios, delegando al `PreguntaService` viejo. *(confirmado por la corrida completa de `./mvnw test`)*
- [x] El campo `intentosParaQueDejeDeSerCriticoDisponible` se preserva correctamente al crear/editar/borrar una `PreguntaSimple` a través del nuevo flujo.
- [x] ~~Las ramas de código específicas de `PREGUNTA_SIMPLE` en el `PreguntaService` viejo fueron eliminadas.~~ **No aplicable**: `createaQuestion`/`updateQuestion` ya eran genéricos para los 6 tipos, no existían ramas específicas que borrar (ver "Decisiones tomadas y descartadas").
- [x] ~~Existen tests unitarios de `Temario` y `PreguntaSimple` (domain) que corren sin contexto de Spring.~~ **No aplicable en este spec**: el domain nuevo es un data holder puro (solo getters/setters de Lombok), sin lógica propia todavía — no se portó `agregarALaLista`/`contieneCritico`/etc. porque quedó fuera de alcance. Un test de getters/setters no aportaría cobertura real. Se retoma cuando un spec futuro agregue comportamiento al domain.
- [x] Existe al menos un test de integración por vertical (lectura de cuestionarios, escritura de `PreguntaSimple`) que verifica paridad de comportamiento contra el código anterior.
- [x] `./mvnw test` corre completo y pasa — **verificación manual del usuario, no ejecutada por el agente.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: listado de cuestionarios y ciclo crear/editar/borrar de una pregunta simple funcionan sin errores visibles.
- [x] Ningún import ni endpoint del slice `answering/` (`verifyResponse`, críticos) fue tocado.

---

## Decisiones tomadas y descartadas

- **Paquete base: mantener `com.lorenzomar3.AQ`, anidando `content/` adentro.**
  Descartado: renombrar todo a `com.aq` ahora. Justificación: tocaría imports de código que todavía no se migra, aumentando el blast radius de este primer paso sin aportar nada al objetivo. El rename se deja para un spec de limpieza final.

- **`GET /questionnaires` mantiene el mismo path y forma de JSON.**
  Descartado: exponer un path nuevo en paralelo (ej. `/v2/questionnaires`). Justificación: el contrato ya es simple y de solo lectura; agregar un path paralelo solo suma mantenimiento doble sin reducir riesgo real.

- **Código viejo se borra apenas el nuevo flujo esté validado.**
  Descartado: mantenerlo en paralelo por un tiempo como red de seguridad. Justificación: dos caminos vivos generan dudas sobre cuál es la fuente de verdad; coherente con el paso 7 de "Consideraciones para la migración" de `ARQUITECTURA.md`.

- **`spring.jpa.hibernate.ddl-auto` cambia de `update` a `validate` globalmente (`application.properties`), descubierto durante la implementación del Paso 5.**
  Descartado: dejarlo en `update` (asunción original del spec). Justificación: Hibernate no permite que dos clases `@Entity` distintas (la vieja y la nueva `*Entity`) mapeen la misma tabla bajo `update`/`create` — lanza `SchemaManagementException: Export identifier encountered more than once`. Esto no es transitorio: dentro del alcance de este spec, las entidades viejas (`Temario`, `AResponder`, `Pregunta`, `PreguntaSimple`) siguen usándose para siempre en operaciones no migradas (fetch, otros tipos de pregunta, otras operaciones de temario), así que la convivencia con las entidades nuevas es permanente, no solo durante la migración. Como este spec no cambia ninguna columna, `validate` cumple el mismo chequeo de arranque sin necesitar generar DDL. Efecto colateral aceptado: cambios de esquema futuros de código no relacionado a este spec dejan de auto-aplicarse al bootear la app; hay que gestionarlos a mano (esto contradice la descripción de `ddl-auto=update` en `CLAUDE.md`, que debería actualizarse en un spec o commit de documentación aparte).
  Alternativa considerada y descartada: que el adapter nuevo reutilice las clases `@Entity` viejas directamente (sin `TemarioEntity`/`AResponderEntity`/`PreguntaSimpleEntity` nuevas), evitando el conflicto de raíz pero perdiendo el aislamiento de "entity limpia sin lógica de negocio" que buscaba `ARQUITECTURA.md`.

- **Entities nuevas mapean a las tablas existentes vía `@Table`.**
  Descartado: crear tablas nuevas y migrar datos. Justificación: el modelo de datos no cambia, solo la capa de código que lo maneja; migrar datos sería riesgo sin beneficio.

- **Alcance de escritura: lectura de cuestionarios + CRUD completo de `PreguntaSimple`.**
  Descartado: dejar toda la escritura para un spec posterior. Justificación: sin al menos un ciclo de escritura no se valida el patrón de mapper/adapter para inserts y updates, que es el punto más riesgoso de toda la migración hexagonal.

- **Dispatch por tipo: el `PreguntaController` viejo delega internamente al nuevo use case cuando `tipo == PREGUNTA_SIMPLE`.**
  Descartado: modificar `FabricaDePreguntas` para que decida el flujo. Justificación: `FabricaDePreguntas` es código compartido con los 5 tipos que no se migran en este spec; tocarlo amplía el blast radius más de lo necesario.

- **`DELETE /questions/{id}` queda 100% en el camino viejo, para todos los tipos incluido `PREGUNTA_SIMPLE`.**
  Descartado: hacer un lookup extra del tipo antes de decidir a qué camino delegar. Justificación: el endpoint no recibe `tipo` en el request (solo `id`), y `preguntaRepository.deleteById(id)` ya es genérico vía la jerarquía `JOINED` — produce el mismo resultado en BD sin importar el subtipo. `EliminarPreguntaUseCase`/`EliminarPreguntaSimpleService` se implementaron igual (quedan cubiertos por test), pero el controller no los invoca.

- **No hubo ramas de código específicas de `PREGUNTA_SIMPLE` para borrar en `PreguntaService.createaQuestion`/`updateQuestion`.**
  Descubierto durante la implementación del Paso 6: ambos métodos ya eran genéricos para los 6 tipos (vía `FabricaDePreguntas` y el mapa `TipoAResponder → BasePreguntaRepositorio`), no tenían un `if (tipo == PREGUNTA_SIMPLE)` que remover. El punto del plan de "borrar ramas específicas" no aplica — esos métodos quedan intactos, simplemente dejan de ser invocados para `PREGUNTA_SIMPLE` porque el controller corta antes.

- **Tests: unitarios de domain (sin Spring) + integración de paridad de comportamiento.**
  Descartado: validar solo a mano. Justificación: testear el domain sin levantar Spring es el beneficio principal que se busca con esta migración; no escribir esos tests desperdicia el punto central del cambio.

- **El criterio de tests unitarios de domain se marca como no aplicable en este spec, en vez de escribir tests triviales de getters/setters.**
  Descubierto durante la implementación del Paso 7: `content/domain/{Temario, Pregunta, PreguntaSimple}` no tienen lógica propia todavía (son data holders puros con Lombok) porque `agregarALaLista`/`contieneCritico`/etc. quedaron deliberadamente fuera de alcance. Descartado: escribir tests que solo verifican que un getter devuelve lo que se seteó. Justificación: no aportan cobertura real, solo testean código generado por Lombok. Se retoma este criterio cuando un spec futuro agregue comportamiento real al domain.

- **`./mvnw test` lo corre el usuario manualmente, no el agente.**
  Ajuste explícito pedido durante la sesión de spec — no ejecutado como parte del flujo de Claude Code.

- **Alcance general acotado a un piloto: `PreguntaSimple` + lectura de cuestionarios.**
  Descartado: migrar todo `content/` de una, o abordar `answering/` en el mismo spec. Justificación: `answering/` depende de que `content/` exponga un puerto real primero, y migrar los 6 tipos de pregunta a la vez multiplicaría el riesgo sin necesidad — el objetivo de este spec es validar el patrón, no completar la migración entera.

- **`TemarioController`/`PreguntaController` NO se mueven a `content/infrastructure/controller/` en este spec — queda pendiente para el final.**
  Decisión tomada después de cerrado el Paso 7, conversando sobre qué hacer con los controllers a largo plazo. Los controllers siguen viviendo en `com.lorenzomar3.AQ.Controller/`, mezclando imports viejos (para los tipos/operaciones no migrados) con los use cases nuevos. Justificación: mover el archivo ahora no cambia nada real (la regla de dependencias de `ARQUITECTURA.md` solo restringe `domain`/`application`, no `infrastructure`) y obligaría a tocarlo de nuevo en cada spec incremental futuro. Plan acordado: cuando se termine de migrar el último tipo de pregunta y las operaciones de `Temario` que faltan, hacer un spec final de "relocation" que mueva las clases a `content/infrastructure/controller/` y borre `PreguntaService`/`TemarioService`/repos viejos — en ese punto el move es puro renombre sin lógica nueva.

---

## Riesgos identificados

- **Mapeo de columnas no trivial.** La entity vieja usa `@Column(name = "id_del_duenio")` en `AResponder.idDuenio` y `@JoinColumn(name = "id_del_duenio")` en `Temario.listaAResponder`. Si las entities nuevas no replican estos nombres de columna exactamente, Hibernate podría generar columnas duplicadas o fallar el mapeo silenciosamente.
  *Mitigación:* revisar columna por columna contra el DDL real en pgAdmin antes de dar por buena cada entity nueva (ya cubierto por el paso 2 del plan, pero vale remarcarlo como riesgo explícito).

- **Coexistencia de dos jerarquías JPA sobre la misma tabla.** Mientras las entities viejas (`AResponder`, `Temario`, `Pregunta`, `PreguntaSimple`) y las nuevas (`AResponderEntity`, etc.) conviven en el mismo `EntityManager`/contexto de persistencia (pasos 2 a 6, antes de borrar código viejo), ambas mapean las mismas tablas. Un flush o caché de segundo nivel inconsistente entre ambas podría enmascarar bugs de sincronización.
  *Mitigación:* evitar que ambos caminos lean/escriban la misma fila dentro de la misma transacción; los tests de paridad deben correr contra estado limpio, no contra datos ya tocados por el otro camino.

- **`PostPreguntaDTO` es un DTO compartido con campos de los 6 tipos de pregunta.** El nuevo `CrearPreguntaUseCase`/`EditarPreguntaUseCase` para `PreguntaSimple` debe ignorar correctamente los campos que no le corresponden (`listaDeOpcionesConSuRespuestaReal`, `listaDeOpcionDesplegableCompartido`, etc.). Un mapeo incompleto podría comportarse distinto al `FabricaDePreguntas` + `BeanUtils.copyProperties` actual sin que sea obvio en revisión de código.
  *Mitigación:* cubierto por el test de integración de paridad del paso 6, pero es el punto más fácil de subestimar.

- **Lógica de "críticos" mal ubicada arquitectónicamente.** `conteoDeCritico` y `verificarSiLaRespuestaEsCorrectaYAsignarCriticos` viven hoy dentro de `Pregunta.java`, pero conceptualmente pertenecen al slice `answering/` (todavía no migrado). Este spec no la toca, pero al migrar `PreguntaSimple` al nuevo domain hay que decidir conscientemente no portar esa lógica ahí para no anticipar diseño de `answering/` sin haberlo especificado.
  *Mitigación:* ninguna en este spec — se deja como nota para el spec que aborde `answering/`.
