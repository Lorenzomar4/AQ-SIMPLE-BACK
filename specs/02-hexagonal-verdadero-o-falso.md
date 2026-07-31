# Spec 02 — Migración hexagonal: CRUD de VerdaderoOFalso (segundo tipo de pregunta)

**Estado:** Implementado y verificado — `./mvnw test` en verde y verificación manual contra `AQ-SIMPLE-FRONT` completada
**Dependencias:** Spec 01 (hexagonal-content-piloto) — reutiliza el patrón de domain/application/infrastructure ya validado ahí
**Fecha:** 2026-07-30

**Objetivo:** Migrar la creación y edición de `VerdaderoOFalso` a arquitectura hexagonal usando interfaces de use case dedicadas por tipo, replicando el patrón validado en el piloto de `PreguntaSimple`, sin tocar `DELETE`, `verifyResponse` ni la lógica de corrección de respuesta.

---

## Alcance

### Incluido

- **Domain puro:** `content.domain.VerdaderoOFalso extends Pregunta` (Java puro, sin anotaciones), agrega el campo `respuestaVerdadera: Boolean`.
- **Infraestructura de persistencia:**
  - `VerdaderoOFalsoEntity extends PreguntaEntity` con `@Table(name = "verdaderoofalso")`.
  - `VerdaderoOFalsoMapper` (`toDomain`/`toEntity`).
  - `VerdaderoOFalsoJpaRepository extends BaseContentRepositorio<VerdaderoOFalsoEntity>`.
  - `VerdaderoOFalsoJpaAdapter implements VerdaderoOFalsoRepositoryPort`.
- **Application — puertos dedicados por tipo** (decisión: no reusar las interfaces de `PreguntaSimple`, ver "Decisiones"):
  - Puerto out: `VerdaderoOFalsoRepositoryPort` (`save`, `findById`, `deleteById`).
  - Puertos in: `CrearVerdaderoOFalsoUseCase`, `EditarVerdaderoOFalsoUseCase`, `EliminarVerdaderoOFalsoUseCase`.
  - Servicios: `CrearVerdaderoOFalsoService`, `EditarVerdaderoOFalsoService`, `EliminarVerdaderoOFalsoService` — mismo patrón que sus equivalentes de `PreguntaSimple` (constructor injection, `idDuenio` seteado directo, sin `Temario.agregarALaLista`).
- **Dispatch en `PreguntaController`:** las ramas condicionales existentes en `POST /questions` y `PUT /questions` (hoy solo evalúan `PREGUNTA_SIMPLE`) se extienden para también evaluar `VERDADERO_FALSO` y delegar a los nuevos use cases. `DELETE /questions/{id}` no se toca — sigue 100% en el camino viejo para todos los tipos, incluido `VERDADERO_FALSO`.
- **`EliminarVerdaderoOFalsoUseCase`/`EliminarVerdaderoOFalsoService`** se implementan igual (cubiertos por test), pero el controller no los invoca — mismo tratamiento que recibió `EliminarPreguntaUseCase` en el spec 01.
- **Tests:** `VerdaderoOFalsoParidadTest` (integración, mismo patrón que `PreguntaSimpleParidadTest`) que compara create/edit contra `PreguntaService.createaQuestion`/`updateQuestion`, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve y que `respuestaVerdadera` persista/actualice igual en ambos caminos.

### Explícitamente NO incluido

- `DELETE /questions/{id}` para `VerdaderoOFalso` — permanece en el camino viejo (`PreguntaService.delete`), igual que para todos los tipos.
- `verifyResponse`, `laRespuestaEsCorrecta` y la lógica de críticos — quedan intactos en el código viejo; pertenecen al slice `answering/`, todavía no migrado.
- Los otros 4 tipos de pregunta restantes (`SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`) — se migran uno a la vez en specs posteriores.
- Cambios en `TipoDeTemario`, `AsignadorDeTipoALasPreguntas` o `FabricaDePreguntas` viejos — el dispatch queda en el controller, no en la fábrica vieja (mismo criterio que el spec 01).
- Cualquier cambio de esquema de base de datos — se reutiliza la tabla existente `verdaderoofalso` sin modificarla.
- Mover `TemarioController`/`PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation" (decisión ya tomada en el spec 01).
- Cambios en `AQ-SIMPLE-FRONT`.
- Generalizar `CrearPreguntaUseCase`/`EditarPreguntaUseCase`/`EliminarPreguntaUseCase` (las de `PreguntaSimple`) para que sirvan a ambos tipos — se opta por interfaces nuevas y dedicadas en su lugar.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` y `PostPreguntaDTO` (ya trae el campo `respuestaVerdadera`) — mismo contrato de wire, sin tocar.
- `content.domain.Pregunta` (creado en el spec 01) — `VerdaderoOFalso` lo extiende sin agregarle nada.
- `content.infrastructure.persistence.entity.PreguntaEntity` (creado en el spec 01) — `VerdaderoOFalsoEntity` lo extiende.
- `BaseContentRepositorio<T extends AResponderEntity>` (creado en el spec 01) — se reusa como base de `VerdaderoOFalsoJpaRepository`.

### `content/domain/` (nuevo)

- **`VerdaderoOFalso extends Pregunta`** — agrega únicamente `respuestaVerdadera: Boolean`. Java puro, sin comportamiento (mismo criterio "anémico" que `PreguntaSimple`: la lógica de corrección no se porta, queda en el código viejo por estar fuera de alcance).

### `content/application/` (nuevo)

- **Puerto out:** `VerdaderoOFalsoRepositoryPort` (`save`, `findById`, `deleteById`).
- **Puertos in:** `CrearVerdaderoOFalsoUseCase`, `EditarVerdaderoOFalsoUseCase`, `EliminarVerdaderoOFalsoUseCase`.
- **Servicios:** `CrearVerdaderoOFalsoService`, `EditarVerdaderoOFalsoService`, `EliminarVerdaderoOFalsoService` — mismo patrón que sus equivalentes de `PreguntaSimple` (constructor injection de `TemarioRepositoryPort` + `VerdaderoOFalsoRepositoryPort`, `idDuenio` seteado directo sin `agregarALaLista`, `intentosParaQueDejeDeSerCriticoDisponible` inicializado en `0` al crear y preservado al editar).

### `content/infrastructure/persistence/` (nuevo)

- **Entity:** `VerdaderoOFalsoEntity extends PreguntaEntity` → `@Table(name = "verdaderoofalso")`. Campo `respuestaVerdadera: Boolean` → columna `respuesta_verdadera` (confirmado contra el DDL real: `create table verdaderoofalso (respuesta_verdadera boolean, id bigint not null primary key constraint ... references pregunta)`).
- **Mapper:** `VerdaderoOFalsoMapper` (`toDomain`/`toEntity`), copia campo a campo explícita, sin MapStruct — mismo estilo que `PreguntaSimpleMapper`.
- **Repository:** `VerdaderoOFalsoJpaRepository extends BaseContentRepositorio<VerdaderoOFalsoEntity>`.
- **Adapter:** `VerdaderoOFalsoJpaAdapter implements VerdaderoOFalsoRepositoryPort`, delega en `VerdaderoOFalsoJpaRepository` + `VerdaderoOFalsoMapper`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. `@Table(name = "verdaderoofalso")` apunta a la tabla ya existente (nombre confirmado por el usuario, sin guiones bajos — no sigue la convención `CamelCaseToUnderscoresNamingStrategy` que sí aplica a `pregunta_simple`).

---

## Plan de implementación

1. **Domain puro.** Crear `content/domain/VerdaderoOFalso.java` (Java puro, extiende `Pregunta`, agrega `respuestaVerdadera: Boolean`). No se conecta a nada todavía — el sistema sigue funcionando exactamente igual.

2. **Entity JPA nueva.** Crear `content/infrastructure/persistence/entity/VerdaderoOFalsoEntity.java` con `@Table(name = "verdaderoofalso")`. Levantar la app y confirmar que arranca sin error (`ddl-auto=validate` falla explícitamente en el boot si el mapeo no coincide con la tabla real).

3. **Mapper.** Crear `VerdaderoOFalsoMapper` (`toDomain`/`toEntity`). Sin uso real todavía.

4. **Puerto y adapter.** Definir `VerdaderoOFalsoRepositoryPort` (out) y su implementación `VerdaderoOFalsoJpaAdapter`, apoyada en `VerdaderoOFalsoJpaRepository extends BaseContentRepositorio<VerdaderoOFalsoEntity>`.

5. **Puertos in y servicios de escritura.** Implementar `CrearVerdaderoOFalsoUseCase`/`CrearVerdaderoOFalsoService`, `EditarVerdaderoOFalsoUseCase`/`EditarVerdaderoOFalsoService`, `EliminarVerdaderoOFalsoUseCase`/`EliminarVerdaderoOFalsoService`, usando la persistence layer del paso 4.

6. **Test de paridad.** Crear `VerdaderoOFalsoParidadTest` (mismo patrón que `PreguntaSimpleParidadTest`): compara `PreguntaService.createaQuestion`/`updateQuestion` contra los nuevos use cases, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve al editar y que `respuestaVerdadera` persista/actualice igual en ambos caminos. Incluye también un test de `EliminarVerdaderoOFalsoUseCase` (creación + borrado, aunque no esté wireado al controller).

7. **Cablear `PreguntaController`.** Recién con el test del paso 6 en verde, extender las ramas condicionales de `POST /questions` y `PUT /questions` (hoy solo evalúan `PREGUNTA_SIMPLE`) para que también evalúen `VERDADERO_FALSO` y deleguen a los nuevos use cases. `DELETE /questions/{id}` no se toca. De paso, confirmar que `PreguntaService.createaQuestion`/`updateQuestion` no tienen ramas específicas de `VERDADERO_FALSO` que remover (hallazgo esperado, igual que con `PreguntaSimple` en el spec 01: probablemente no aplica, esos métodos ya son genéricos).

8. **Verificación final.** Correr `./mvnw test` completo (lo corre el usuario manualmente) y probar contra `AQ-SIMPLE-FRONT`: crear y editar una `VerdaderoOFalso`, y confirmar que responder la pregunta (`verifyResponse`) y borrarla siguen funcionando igual que antes, sin errores visibles.

---

## Criterios de aceptación

- [x] Existe y compila `content/domain/VerdaderoOFalso.java` sin ninguna anotación de Spring/JPA/Jackson.
- [x] Existe `VerdaderoOFalsoEntity` con `@Table(name = "verdaderoofalso")`, extendiendo `PreguntaEntity`.
- [x] Al levantar la app con la entity nueva, no se crean tablas ni columnas nuevas en Postgres (`ddl-auto=validate` arranca sin error de mapeo). *(confirmado indirectamente — `./mvnw test` corrió contra la app levantada con `ddl-auto=validate` y pasó)*
- [x] `POST /questions` con `tipo == VERDADERO_FALSO` produce el mismo resultado (JSON de respuesta y estado persistido en BD) que antes de la migración, **excepto `respuestaVerdadera`** — ver bug preexistente documentado en "Decisiones tomadas y descartadas": el camino viejo nunca la persistía, el camino nuevo la corrige intencionalmente. *(confirmado por `VerdaderoOFalsoParidadTest`, en verde)*
- [x] `PUT /questions` con `tipo == VERDADERO_FALSO` produce el mismo resultado que antes de la migración, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve, **excepto `respuestaVerdadera`** (mismo bug preexistente corregido intencionalmente). *(confirmado por `VerdaderoOFalsoParidadTest`, en verde)*
- [x] `DELETE /questions/{id}` para `VerdaderoOFalso` sigue funcionando sin cambios, por el camino viejo. *(no tocado — `PreguntaController.delete` sigue llamando a `preguntaService.delete`)*
- [x] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con `tipo == PREGUNTA_SIMPLE` o cualquiera de los otros 4 tipos restantes siguen funcionando sin cambios. *(las ramas nuevas son adicionales — `else` sigue delegando a `preguntaService` igual que antes)*
- [x] `verifyResponse` para `VerdaderoOFalso` sigue funcionando sin cambios (no tocado por este spec).
- [x] Existe `VerdaderoOFalsoParidadTest`, con al menos un test de paridad para creación y otro para edición, contra el comportamiento del `PreguntaService` anterior.
- [x] `./mvnw test` corre completo y pasa — verificación manual del usuario, no ejecutada por el agente.
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: crear, editar, responder y borrar una `VerdaderoOFalso` funcionan sin errores visibles.
- [x] Ningún import ni endpoint del slice `answering/` (`verifyResponse`, críticos) fue tocado.
- [x] Las interfaces `CrearPreguntaUseCase`/`EditarPreguntaUseCase`/`EliminarPreguntaUseCase` (las de `PreguntaSimple`) no fueron modificadas.

---

## Decisiones tomadas y descartadas

- **Interfaces de use case dedicadas por tipo** (`CrearVerdaderoOFalsoUseCase`, `EditarVerdaderoOFalsoUseCase`, `EliminarVerdaderoOFalsoUseCase`), en vez de reusar o generalizar las de `PreguntaSimple`.
  Descartado: generalizar `CrearPreguntaUseCase`/`EditarPreguntaUseCase`/`EliminarPreguntaUseCase` con `@Qualifier` o un parámetro de tipo. Justificación: evita tocar código ya validado del spec 01 y sigue el mismo criterio de "no generalizar prematuramente" que se aplicó ahí; el costo de duplicar la forma de la interfaz es bajo comparado con el riesgo de romper el wiring existente de `PreguntaSimple`.

- **`DELETE /questions/{id}` para `VerdaderoOFalso` queda 100% en el camino viejo**, igual que con `PreguntaSimple`.
  Descartado: agregar un lookup de tipo antes de decidir a qué camino delegar. Justificación: mismo razonamiento que el spec 01 — el endpoint no recibe `tipo` en el request, y `deleteById` ya es genérico vía la jerarquía JOINED.

- **`verifyResponse` y `laRespuestaEsCorrecta` quedan fuera de alcance**, igual que en el spec 01.
  Descartado: portar la lógica de corrección al domain nuevo en este spec. Justificación: pertenece conceptualmente al slice `answering/`, que depende de que `content/` esté migrado primero; ampliar el alcance ahora mezclaría dos slices distintos.

- **Nombre de tabla: `verdaderoofalso`, confirmado directamente por el usuario contra el DDL real** (no sigue la convención `CamelCaseToUnderscoresNamingStrategy` que sí aplica a `pregunta_simple`).
  Descartado: inferir el nombre por convención y arriesgarse a que `ddl-auto=validate` falle al arrancar. Justificación: el usuario tiene acceso directo a la BD real; confirmar de entrada evita un ciclo de prueba y error en el paso 2 del plan.

- **Los servicios de creación/edición no usan `Temario.agregarALaLista()`**, replicando el patrón de `CrearPreguntaSimpleService` (solo setean `idDuenio`).
  Descartado: forzar paridad estricta con el flujo viejo llamando a `agregarALaLista()`. Justificación: el resultado en BD es idéntico (misma columna FK, escrita por otro camino), ya validado en el spec 01; la única diferencia es de estado en memoria dentro de la misma transacción, que hoy nadie lee sincrónicamente en el camino migrado. Mantener el precedente evita que el domain nuevo dependa de la entidad `Temario` vieja.

- **No hubo ramas de código específicas de `VERDADERO_FALSO` para remover en `PreguntaService.createaQuestion`/`updateQuestion`.**
  Confirmado durante la implementación del paso 7 (mismo hallazgo que con `PreguntaSimple` en el spec 01): ambos métodos ya eran genéricos vía `FabricaDePreguntas` y el mapa `TipoAResponder → BasePreguntaRepositorio`. No se removió nada de `PreguntaService`; simplemente deja de ser invocado para `VERDADERO_FALSO` porque el controller corta antes.

- **Bug preexistente descubierto en el paso 6 (`VerdaderoOFalsoParidadTest`): el flujo viejo nunca persiste `respuestaVerdadera`.** Tanto `FabricaDePreguntas.fromJSON()` (create) como `PreguntaService.updateQuestion()` (edit) usan `BeanUtils.copyProperties(preguntaDTO, pregunta)`, donde `preguntaDTO` es un `record` (`PostPreguntaDTO`). El accesor de un record se llama `respuestaVerdadera()`, no `getRespuestaVerdadera()`/`isRespuestaVerdadera()` — la convención JavaBean que espera `java.beans.Introspector` (usado internamente por `BeanUtils`). Como consecuencia, el campo nunca se copia: en creación queda `null` en BD; en edición queda intacto en el valor de la creación, ignorando lo que mande el DTO de edición. Esto rompe silenciosamente `verifyResponse`/`laRespuestaEsCorrecta` para `VerdaderoOFalso` por el camino viejo, para cualquier pregunta creada o editada a través de él.
  **Decisión (confirmada con el usuario):** el flujo nuevo hexagonal **no replica el bug** — `CrearVerdaderoOFalsoService`/`EditarVerdaderoOFalsoService` setean `respuestaVerdadera` explícitamente a partir del DTO, igual que ya se hacía con `respuestaEstablecida` en `PreguntaSimple`. Esto es intencionalmente una corrección de bug, no una regresión de paridad. `VerdaderoOFalsoParidadTest` documenta el comportamiento buggy del camino viejo con asserts explícitos (en vez de comparar campo a campo contra el viejo) y verifica que el camino nuevo persiste/actualiza el valor correcto.
  Descartado: replicar el bug para lograr paridad estricta byte a byte, o arreglarlo también en el código viejo (`FabricaDePreguntas`/`PreguntaService`) dentro de este spec. Justificación: perpetuar un bug conocido no tiene sentido una vez detectado, y arreglar el camino viejo amplía el alcance a código compartido con los 4 tipos de pregunta que todavía no se migran — mayor blast radius del que este spec busca.
  *Impacto en producción:* cualquier `VerdaderoOFalso` existente creada/editada por el camino viejo antes de esta migración probablemente tiene `respuestaVerdadera = null` en BD y no puede responderse correctamente hasta que se edite una vez a través del nuevo flujo (`PUT /questions`, que a partir de este spec usa el camino corregido).

---

## Riesgos identificados

- **Coexistencia de dos jerarquías JPA sobre la misma tabla `verdaderoofalso`** (la vieja `VerdaderoOFalso` `@Entity` y la nueva `VerdaderoOFalsoEntity`), mientras conviven durante los pasos 2 a 7. Mismo riesgo ya identificado en el spec 01 para `PreguntaSimple`.
  *Mitigación:* evitar que ambos caminos lean/escriban la misma fila dentro de la misma transacción; el test de paridad corre contra estado limpio, no contra datos ya tocados por el otro camino.

- **`PreguntaRepository` (genérico, tipado a `Pregunta`) sigue siendo necesario para `VERDADERO_FALSO`** después de esta migración, porque `verifyResponse`, `DELETE` y el `updateQuestion` legacy no se tocan y siguen dependiendo de la entrada `TipoAResponder.VERDADERO_FALSO → preguntaRepository` en `PreguntaService.mapDeRepositorios`.
  *Mitigación:* dejar documentado explícitamente (este spec) que esa entrada del mapa debe permanecer hasta que un spec futuro migre también `DELETE` y `verifyResponse` — borrarla prematuramente rompería esos dos caminos para `VerdaderoOFalso`.
