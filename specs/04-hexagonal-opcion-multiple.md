# Spec 04 — Migración hexagonal: CRUD de OpcionMultiple (cuarto tipo de pregunta)

**Estado:** Implementado — `./mvnw test` en verde y verificación manual contra `AQ-SIMPLE-FRONT` completada
**Dependencias:** Spec 01 (hexagonal-content-piloto), Spec 03 (hexagonal-seleccion-unica) — reutiliza tal cual `content.domain.Opcion`, `OpcionEntity` y `OpcionMapper` creados ahí; mismo patrón de colección hija validado con `SeleccionUnica`
**Fecha:** 2026-07-30

**Objetivo:** Migrar la creación y edición de `OpcionMultiple` a arquitectura hexagonal, reutilizando el `Opcion`/`OpcionEntity`/`OpcionMapper` ya creados para `SeleccionUnica` y agregando únicamente una `OpcionMultipleEntity` propia (`@JoinColumn(name = "id_multiple_opcion")` sobre la tabla compartida `opcion`) más los puertos y servicios dedicados de escritura, sin tocar `DELETE`, `verifyResponse` ni el `@PostLoad` que baraja las opciones en el modelo viejo.

---

## Alcance

### Incluido

- **Domain puro:**
  - `content.domain.OpcionMultiple extends Pregunta` — nuevo, agrega `listaDeOpciones: List<Opcion>` (usando el mismo `content.domain.Opcion` de spec 03, sin cambios). Sin comportamiento (mismo criterio "anémico" que los tipos anteriores): no se porta `validacionDeDatosDTO` (que en el modelo viejo ya es un no-op) ni el `@PostLoad` que baraja (`Collections.shuffle`) la lista al leer — ese comportamiento pertenece a la lectura/respuesta, fuera de alcance.
  - El nombre del campo es `listaDeOpciones` (no `listaDeOpcionesConSuRespuestaReal`, el nombre del modelo viejo) por consistencia con `content.domain.SeleccionUnica` — decisión confirmada con el usuario, ver "Decisiones".
- **Infraestructura de persistencia:**
  - `OpcionMultipleEntity extends PreguntaEntity` → `@Table(name = "opcion_multiple")`, con `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_multiple_opcion") List<OpcionEntity> listaDeOpciones` — mismo criterio EAGER que `SeleccionUnicaEntity` (spec 03), mismo motivo: este repository solo lo usan los use cases de escritura, que necesitan la lista completa dentro de la transacción.
  - `OpcionMultipleMapper` (`toDomain`/`toEntity`), delega en el `OpcionMapper` ya existente para la lista — mismo patrón que `SeleccionUnicaMapper`, sin tocarlo.
  - `OpcionMultipleJpaRepository extends BaseContentRepositorio<OpcionMultipleEntity>`.
  - `OpcionMultipleJpaAdapter implements OpcionMultipleRepositoryPort`.
  - **Nada de esto toca `OpcionEntity` ni `OpcionMapper`** — se reutilizan tal cual, sin ningún cambio (ni siquiera de nombre de método), porque ya son genéricos respecto a la columna de join que los persiste en cascada.
- **Application — puertos dedicados por tipo** (mismo criterio que specs 02 y 03: no generalizar):
  - Puerto out: `OpcionMultipleRepositoryPort` (`save`, `findById`, `deleteById`).
  - Puertos in: `CrearOpcionMultipleUseCase`, `EditarOpcionMultipleUseCase`, `EliminarOpcionMultipleUseCase`.
  - Servicios: `CrearOpcionMultipleService`, `EditarOpcionMultipleService`, `EliminarOpcionMultipleService` — mismo patrón que `SeleccionUnica`: `idDuenio` seteado directo (sin `Temario.agregarALaLista`), `intentosParaQueDejeDeSerCriticoDisponible` inicializado en `0` al crear y preservado al editar. Conversión manual `List<model...TiposDePreguntas.Opcion>` (del DTO, mismo campo de wire `listaDeOpcionesConSuRespuestaReal` que ya usa `SeleccionUnica`) → `List<content.domain.Opcion>`, campo a campo, igual que en `Crear/EditarSeleccionUnicaService`.
  - **Edición: reemplazo completo de la lista de opciones**, igual que `SeleccionUnica` — mismo argumento de *bag semantics* de Hibernate (confirmado con el usuario).
- **Dispatch en `PreguntaController`:** las ramas condicionales de `POST /questions` y `PUT /questions` se extienden para evaluar también `OPCION_MULTIPLE` y delegar a los nuevos use cases. `DELETE /questions/{id}` no se toca — sigue 100% en el camino viejo, igual que para los 3 tipos ya migrados (confirmado con el usuario).
- **`EliminarOpcionMultipleUseCase`/`EliminarOpcionMultipleService`** se implementan igual (cubiertos por test), pero el controller no los invoca — mismo tratamiento que los tipos anteriores.
- **Tests:** `OpcionMultipleParidadTest` (integración, mismo patrón que `SeleccionUnicaParidadTest`) que compara create/edit contra `PreguntaService.createaQuestion`/`updateQuestion`, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve y un ciclo de edición que cambie el contenido de la lista de opciones (agregar/quitar/modificar), confirmado con el usuario.

### Explícitamente NO incluido

- `DELETE /questions/{id}` para `OpcionMultiple` — permanece en el camino viejo.
- `verifyResponse`, `laRespuestaEsCorrecta`, `validacionDeDatosDTO` y la lógica de críticos — quedan intactos en el código viejo; pertenecen al slice `answering/`, todavía no migrado.
- El `@PostLoad` de `Collections.shuffle(listaDeOpcionesConSuRespuestaReal)` del modelo viejo — es comportamiento de lectura (orden aleatorio al mostrar la pregunta), no de escritura; sigue existiendo tal cual en `model.AResponder.TiposDePreguntas.OpcionMultiple`, no se porta al domain nuevo.
- Los tipos de pregunta restantes (`DesplegableCompartido` y `DesplegableIndependiente`) — se migran uno a la vez en specs posteriores, mismo criterio.
- Cambios en `TipoDeTemario`, `AsignadorDeTipoALasPreguntas` o `FabricaDePreguntas` viejos — incluida la rama vacía y aparentemente muerta en `FabricaDePreguntas.fromJSON` (`if (pregunta.getTipo().equals(TipoAResponder.OPCION_MULTIPLE)) { OpcionMultiple opcionMultiple = (OpcionMultiple) pregunta; }`, sin efecto) — el dispatch queda en el controller, no se toca ni se limpia ese código muerto en este spec.
- Cualquier cambio de esquema de base de datos — se reutilizan las tablas existentes (`opcion_multiple`, `opcion`) sin modificarlas, confirmado contra el DDL real provisto por el usuario.
- Mover `TemarioController`/`PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Merge por id de la lista de opciones al editar — se opta por reemplazo completo, mismo criterio que `SeleccionUnica`.
- Cambios en `OpcionEntity`/`OpcionMapper` — se reutilizan sin ninguna modificación.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` y `PostPreguntaDTO` (ya trae `listaDeOpcionesConSuRespuestaReal: List<Opcion>`, el mismo campo de wire que ya usa `SeleccionUnica`) — mismo contrato, sin tocar.
- `content.domain.Pregunta`, `content.domain.Opcion` (creados en specs 01 y 03) — `OpcionMultiple` extiende `Pregunta` y reutiliza `Opcion` sin cambios.
- `content.infrastructure.persistence.entity.PreguntaEntity`, `OpcionEntity`, `content.infrastructure.persistence.mapper.OpcionMapper`, `BaseContentRepositorio<T>` (creados en specs 01 y 03) — se reutilizan sin ninguna modificación.

### `content/domain/` (nuevo)

- **`OpcionMultiple extends Pregunta`** — agrega `listaDeOpciones: List<Opcion>` (usando el `Opcion` de domain ya existente). Sin comportamiento.

### `content/application/` (nuevo)

- **Puerto out:** `OpcionMultipleRepositoryPort` (`save`, `findById`, `deleteById`).
- **Puertos in:** `CrearOpcionMultipleUseCase`, `EditarOpcionMultipleUseCase`, `EliminarOpcionMultipleUseCase`.
- **Servicios:** `CrearOpcionMultipleService`, `EditarOpcionMultipleService`, `EliminarOpcionMultipleService`.

### `content/infrastructure/persistence/` (nuevo)

- **Entity:** `OpcionMultipleEntity extends PreguntaEntity` → `@Table(name = "opcion_multiple")`, con `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_multiple_opcion") List<OpcionEntity> listaDeOpciones`.
- **Mapper:** `OpcionMultipleMapper` (`toDomain`/`toEntity`, delega en `OpcionMapper` para la lista, copia campo a campo explícita, sin MapStruct).
- **Repository:** `OpcionMultipleJpaRepository extends BaseContentRepositorio<OpcionMultipleEntity>`.
- **Adapter:** `OpcionMultipleJpaAdapter implements OpcionMultipleRepositoryPort`, delega en `OpcionMultipleJpaRepository` + `OpcionMultipleMapper`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. Confirmado contra el DDL real provisto por el usuario: `opcion_multiple` (única columna propia: `id`, FK a `pregunta`) y `opcion` (`id`, `la_respuesta_es`, `opcion`, `id_multiple_opcion`, más `id_seleccion_unica` e `id_desplegable_independiente` — columnas de los otros dos tipos que comparten la tabla y que no se tocan en este spec).

---

## Plan de implementación

1. **Nombre de tabla confirmado** contra el DDL real (`opcion_multiple`, columna de join `id_multiple_opcion` en `opcion`) — ver "Modelo de datos".
2. **Domain puro.** Crear `content/domain/OpcionMultiple.java` (extiende `Pregunta`, agrega `listaDeOpciones: List<Opcion>`, reutilizando el `Opcion` de spec 03). No se conecta a nada todavía.
3. **Entity JPA nueva.** Crear `OpcionMultipleEntity`. Levantar la app y confirmar que arranca sin error de mapeo (`ddl-auto=validate`). No se toca `OpcionEntity`.
4. **Mapper.** Crear `OpcionMultipleMapper`, delegando en el `OpcionMapper` existente para la lista.
5. **Puerto y adapter.** Definir `OpcionMultipleRepositoryPort` y `OpcionMultipleJpaAdapter`, apoyado en `OpcionMultipleJpaRepository`.
6. **Puertos in y servicios de escritura.** Implementar `CrearOpcionMultipleUseCase`/`Service`, `EditarOpcionMultipleUseCase`/`Service` (reemplazo completo de opciones), `EliminarOpcionMultipleUseCase`/`Service`.
7. **Test de paridad.** Crear `OpcionMultipleParidadTest`: compara `PreguntaService.createaQuestion`/`updateQuestion` contra los nuevos use cases, incluyendo verificación de que la lista de opciones (contenido: texto + `laRespuestaEs`) persiste/actualiza de forma equivalente, un ciclo de edición que cambie el contenido de la lista, y que `intentosParaQueDejeDeSerCriticoDisponible` se preserva al editar.
8. **Cablear `PreguntaController`.** Extender las ramas condicionales de `POST /questions` y `PUT /questions` para evaluar también `OPCION_MULTIPLE`. `DELETE /questions/{id}` no se toca.
9. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: crear y editar una `OpcionMultiple` (con sus opciones), confirmar que responder (`verifyResponse`) y borrar siguen funcionando igual que antes.

---

## Criterios de aceptación

- [x] Existe y compila `content/domain/OpcionMultiple.java` sin ninguna anotación de Spring/JPA/Jackson.
- [x] Existe `OpcionMultipleEntity` (`@Table(name = "opcion_multiple")`, extiende `PreguntaEntity`), con nombre de tabla confirmado contra la BD real.
- [x] Al levantar la app con la entity nueva, no se crean tablas ni columnas nuevas en Postgres (`ddl-auto=validate` arranca sin error de mapeo). *(confirmado por el usuario al levantar la app en el paso 3)*
- [x] `POST /questions` con `tipo == OPCION_MULTIPLE` persiste la pregunta y su lista de opciones (texto + `laRespuestaEs`), produciendo un resultado equivalente al flujo viejo. *(cubierto por `OpcionMultipleParidadTest`, en verde)*
- [x] `PUT /questions` con `tipo == OPCION_MULTIPLE` reemplaza completamente la lista de opciones existentes por la del DTO de edición, preservando `intentosParaQueDejeDeSerCriticoDisponible`. *(cubierto por `OpcionMultipleParidadTest`, en verde)*
- [x] `DELETE /questions/{id}` para `OpcionMultiple` sigue funcionando sin cambios, por el camino viejo. *(no tocado — `PreguntaController.delete` sigue llamando a `preguntaService.delete`; confirmado además por verificación manual)*
- [x] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con los demás tipos siguen funcionando sin cambios. *(las ramas nuevas son adicionales; confirmado por `./mvnw test` completo en verde)*
- [x] `verifyResponse` para `OpcionMultiple` sigue funcionando sin cambios (no tocado por este spec). *(confirmado por verificación manual contra `AQ-SIMPLE-FRONT`)*
- [x] Existe `OpcionMultipleParidadTest`, con al menos un test de paridad para creación, edición (verificando reemplazo de opciones) y borrado.
- [x] `./mvnw test` corre completo y pasa — **verificación manual del usuario, no ejecutada por el agente.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: crear, editar, responder y borrar una `OpcionMultiple` funcionan sin errores visibles. **(confirmado por el usuario)**
- [x] Ningún import ni endpoint del slice `answering/` fue tocado.
- [x] Las interfaces/entities/mappers de `PreguntaSimple`, `VerdaderoOFalso` y `SeleccionUnica` no fueron modificadas, incluyendo `OpcionEntity`/`OpcionMapper` (reutilizados sin cambios).

---

## Decisiones tomadas y descartadas

- **`OpcionEntity` y `OpcionMapper` (creados en spec 03) se reutilizan tal cual, sin ningún cambio.**
  Descartado: crear una `OpcionMultipleEntity` de opciones separada, o parametrizar `OpcionEntity` con la columna de join. Justificación: `OpcionEntity` ya es una entity plana sin relación bidireccional a su padre — es agnóstica de qué `@JoinColumn` la referencia. Es el mismo criterio que ya adelantaba spec 03 en su sección de decisiones ("`OpcionEntity` nueva puede reutilizarse en el spec de `OpcionMultiple` sin cambios").

- **El campo del domain nuevo se llama `listaDeOpciones`, no `listaDeOpcionesConSuRespuestaReal`** (el nombre del modelo viejo).
  Confirmado con el usuario tras preguntar el motivo. Justificación: el domain nuevo ya está desacoplado del wire DTO (`PostPreguntaDTO.listaDeOpcionesConSuRespuestaReal` sigue siendo el contrato externo, sin cambios) — la conversión entre ambos ya es manual en el servicio, igual que en `SeleccionUnica`. Usar el mismo nombre de campo (`listaDeOpciones`) en `OpcionMultiple` y `SeleccionUnica` mantiene mapper y servicios simétricos entre los dos tipos que comparten el mismo patrón de colección hija, sin ganancia alguna en replicar el nombre viejo.
  Descartado: mantener `listaDeOpcionesConSuRespuestaReal` para facilitar comparación directa con el código viejo durante la migración.

- **Edición de opciones: reemplazo completo, no merge por id.** Mismo criterio y misma justificación que `SeleccionUnica` (spec 03): colección `List` sin `@OrderColumn` = semántica de *bag* en Hibernate (borra y reinserta todo el contenido en cada `save()`).

- **`OpcionMultipleEntity.listaDeOpciones` es `FetchType.EAGER`**, igual que `SeleccionUnicaEntity` (mismo motivo: este repository solo lo usan los use cases de escritura, que necesitan la lista completa en la misma transacción; los endpoints de lectura siguen sirviéndose del modelo viejo, sin cambios).

- **Interfaces de use case dedicadas por tipo**, siguiendo el criterio ya establecido en specs 02 y 03.

- **`DELETE /questions/{id}` queda 100% en el camino viejo**, mismo razonamiento que specs 01, 02 y 03.

- **La rama vacía de `OPCION_MULTIPLE` en `FabricaDePreguntas.fromJSON` no se toca ni se limpia.**
  Descartado: eliminar el código muerto (`if (pregunta.getTipo().equals(TipoAResponder.OPCION_MULTIPLE)) { OpcionMultiple opcionMultiple = (OpcionMultiple) pregunta; }`, sin efecto observable) como parte de este spec. Justificación: ese método deja de invocarse para `OPCION_MULTIPLE` en cuanto el controller corta antes (mismo hallazgo que specs 01/02 con `PreguntaSimple`/`VerdaderoOFalso`); limpiar código muerto en la fábrica vieja es un cambio cosmético fuera del criterio de "migración sin tocar lo que no haga falta" que vienen siguiendo estos specs.

- **No se porta el `@PostLoad` de `Collections.shuffle`.**
  Descartado: replicar el barajado de opciones en el domain nuevo. Justificación: es comportamiento de lectura (afecta el orden en que se muestran las opciones al responder), no de escritura — pertenece al slice `answering/`/lectura, todavía no migrado; portarlo ahora ampliaría el alcance de este spec sin necesidad.

---

## Riesgos identificados

- **Bag semantics de Hibernate en la colección `Opcion`, ahora compartida entre dos `@JoinColumn` distintas (`id_seleccion_unica` e `id_multiple_opcion`) sobre la misma tabla `opcion`.** Un mapeo incorrecto de la columna de join en `OpcionMultipleEntity` podría, en el peor caso, hacer que Hibernate reasigne o pise filas de `Opcion` que en realidad pertenecen a una `SeleccionUnica` (o viceversa).
  *Mitigación:* verificar en el paso 3 que `@JoinColumn(name = "id_multiple_opcion")` apunta a la columna correcta (ya confirmada contra el DDL real) y que `OpcionMultipleJpaRepository`/`SeleccionUnicaJpaRepository` no comparten ningún query cruzado; el test de paridad del paso 7 debe correr contra estado limpio.

- **Conversión manual `Opcion` (vieja, del DTO) → `content.domain.Opcion` (nueva).** Mismo riesgo ya identificado en spec 03 para `SeleccionUnica`: lógica nueva que no existe en el flujo viejo, un mapeo incompleto de campos podría producir un resultado sutilmente distinto sin ser obvio en revisión de código.
  *Mitigación:* cubierto por el test de paridad del paso 7, comparando contenido de opciones (texto + `laRespuestaEs`) entre ambos caminos.

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas `opcion_multiple`/`opcion`**, mismo riesgo ya identificado en specs 01, 02 y 03.
  *Mitigación:* mismo criterio — test de paridad corre contra estado limpio, no contra datos ya tocados por el otro camino.
