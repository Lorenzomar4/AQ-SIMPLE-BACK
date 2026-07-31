# Spec 03 — Migración hexagonal: CRUD de SeleccionUnica (tercer tipo de pregunta)

**Estado:** Implementado — pendiente `./mvnw test` y verificación manual contra `AQ-SIMPLE-FRONT` (a cargo del usuario)
**Dependencias:** Spec 01 (hexagonal-content-piloto), Spec 02 (hexagonal-verdadero-o-falso) — reutiliza el mismo patrón de domain/application/infrastructure
**Fecha:** 2026-07-30

**Objetivo:** Migrar la creación y edición de `SeleccionUnica` a arquitectura hexagonal, replicando el patrón validado en `PreguntaSimple` y `VerdaderoOFalso`, con la complejidad adicional de que `SeleccionUnica` tiene una colección hija (`List<Opcion>`) que debe crearse/reemplazarse junto con la pregunta.

---

## Alcance

### Incluido

- **Domain puro:**
  - `content.domain.Opcion` — nuevo, Java puro (`id: Long`, `opcion: String`, `laRespuestaEs: Boolean`). Es una clase nueva en el domain nuevo, no reutiliza la `Opcion` JPA vieja (`model.AResponder.TiposDePreguntas.Opcion`), aunque esa misma clase vieja es el tipo declarado en `PostPreguntaDTO.listaDeOpcionesConSuRespuestaReal` (el DTO de wire no cambia — ver "Modelo de datos").
  - `content.domain.SeleccionUnica extends Pregunta` — agrega `listaDeOpciones: List<Opcion>` (usando el `Opcion` de domain nuevo). Sin comportamiento (mismo criterio "anémico" que `PreguntaSimple`/`VerdaderoOFalso`): no se porta `validacionDeOpcionUnica`/`existeUnaOpcionVerdaderaUnicamente`/`laRespuestaEsCorrecta`, que pertenecen a `answering/` y no se invocan durante creación/edición en el flujo actual (confirmado leyendo `FabricaDePreguntas.fromJSON`, que no valida nada al crear).
- **Infraestructura de persistencia:**
  - `OpcionEntity` — nueva, `@Entity @Table(name = "opcion")` (pendiente de confirmación, ver "Riesgos"), con `id/opcion/laRespuestaEs`. No tiene relación bidireccional a su padre (unidireccional, igual que el modelo viejo).
  - `SeleccionUnicaEntity extends PreguntaEntity` → `@Table(name = "seleccion_unica")` (pendiente de confirmación), con `@OneToMany(cascade = CascadeType.ALL) @JoinColumn(name = "id_seleccion_unica") List<OpcionEntity> listaDeOpciones`.
  - `OpcionMapper` (`toDomain`/`toEntity` para `Opcion`), `SeleccionUnicaMapper` (`toDomain`/`toEntity`, delega en `OpcionMapper` para la lista).
  - `SeleccionUnicaJpaRepository extends BaseContentRepositorio<SeleccionUnicaEntity>`.
  - `SeleccionUnicaJpaAdapter implements SeleccionUnicaRepositoryPort`.
- **Application — puertos dedicados por tipo** (mismo criterio que spec 02: no generalizar con las interfaces de `PreguntaSimple`/`VerdaderoOFalso`):
  - Puerto out: `SeleccionUnicaRepositoryPort` (`save`, `findById`, `deleteById`).
  - Puertos in: `CrearSeleccionUnicaUseCase`, `EditarSeleccionUnicaUseCase`, `EliminarSeleccionUnicaUseCase`.
  - Servicios: `CrearSeleccionUnicaService`, `EditarSeleccionUnicaService`, `EliminarSeleccionUnicaService` — mismo patrón de `idDuenio` seteado directo (sin `Temario.agregarALaLista`), `intentosParaQueDejeDeSerCriticoDisponible` inicializado en `0` al crear y preservado al editar. La conversión `List<model.AResponder.TiposDePreguntas.Opcion>` (del DTO) → `List<content.domain.Opcion>` se hace a mano dentro del servicio (mapeo campo a campo: `opcion.getOpcion()`/`opcion.getRespuestaCorrecta()` → `laRespuestaEs`), igual que ya se hace con `respuestaVerdadera` en `VerdaderoOFalso`.
  - **Edición: reemplazo completo de la lista de opciones.** `EditarSeleccionUnicaService` descarta la lista de `Opcion` existente y persiste la que venga en el DTO de edición (decisión confirmada con el usuario — ver "Decisiones"). Esto replica el comportamiento del flujo viejo: `List<Opcion>` sin `@OrderColumn` usa semántica de *bag* en Hibernate, que en cada `save()` borra y reinserta todo el contenido de la colección en función del estado nuevo del campo, no hace *merge* fila a fila.
- **Dispatch en `PreguntaController`:** las ramas condicionales de `POST /questions` y `PUT /questions` se extienden para evaluar también `SELECCION_UNICA` y delegar a los nuevos use cases. `DELETE /questions/{id}` no se toca — sigue 100% en el camino viejo, igual que para `PreguntaSimple` y `VerdaderoOFalso`.
- **`EliminarSeleccionUnicaUseCase`/`EliminarSeleccionUnicaService`** se implementan igual (cubiertos por test), pero el controller no los invoca — mismo tratamiento que los tipos anteriores.
- **Tests:** `SeleccionUnicaParidadTest` (integración, mismo patrón que `VerdaderoOFalsoParidadTest`) que compara create/edit contra `PreguntaService.createaQuestion`/`updateQuestion`, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve y que la lista de opciones persista/actualice de forma equivalente en ambos caminos (contenido, no necesariamente los mismos ids tras un edit, dado el reemplazo completo).

### Explícitamente NO incluido

- `DELETE /questions/{id}` para `SeleccionUnica` — permanece en el camino viejo.
- `verifyResponse`, `laRespuestaEsCorrecta`, `validacionDeOpcionUnica`/`existeUnaOpcionVerdaderaUnicamente` y la lógica de críticos — quedan intactos en el código viejo; pertenecen al slice `answering/`, todavía no migrado.
- `OpcionMultiple` — comparte la clase `Opcion` y el patrón `IPreguntaVariasOpciones`, pero es un tipo de pregunta distinto (`TipoAResponder.OPCION_MULTIPLE`) con su propio `@JoinColumn` (`id_multiple_opcion`) sobre la misma tabla `opcion`. Se migra en un spec posterior, uno a la vez, mismo criterio que specs 01/02.
- Los 2 tipos de pregunta restantes (`DesplegableCompartido`, `DesplegableIndependiente`) — se migran uno a la vez en specs posteriores.
- Cambios en `TipoDeTemario`, `AsignadorDeTipoALasPreguntas` o `FabricaDePreguntas` viejos — el dispatch queda en el controller.
- Cualquier cambio de esquema de base de datos — se reutilizan las tablas existentes sin modificarlas.
- Mover `TemarioController`/`PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Merge por id de la lista de opciones al editar (ver "Decisiones") — se opta por reemplazo completo, no por preservar ids de opciones existentes.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` y `PostPreguntaDTO` (ya trae `listaDeOpcionesConSuRespuestaReal: List<Opcion>`, usando la `Opcion` JPA vieja como tipo de wire) — mismo contrato, sin tocar.
- `content.domain.Pregunta`, `content.infrastructure.persistence.entity.PreguntaEntity`, `BaseContentRepositorio<T>` (creados en spec 01) — `SeleccionUnica`/`SeleccionUnicaEntity` los extienden igual que los tipos anteriores.

### `content/domain/` (nuevo)

- **`Opcion`** — Java puro: `id: Long`, `opcion: String`, `laRespuestaEs: Boolean`.
- **`SeleccionUnica extends Pregunta`** — agrega `listaDeOpciones: List<Opcion>`. Sin comportamiento.

### `content/application/` (nuevo)

- **Puerto out:** `SeleccionUnicaRepositoryPort` (`save`, `findById`, `deleteById`).
- **Puertos in:** `CrearSeleccionUnicaUseCase`, `EditarSeleccionUnicaUseCase`, `EliminarSeleccionUnicaUseCase`.
- **Servicios:** `CrearSeleccionUnicaService`, `EditarSeleccionUnicaService`, `EliminarSeleccionUnicaService`.

### `content/infrastructure/persistence/` (nuevo)

- **Entity:** `OpcionEntity` → `@Entity @Table(name = "opcion")` (a confirmar), campos `id`/`opcion`/`laRespuestaEs`.
- **Entity:** `SeleccionUnicaEntity extends PreguntaEntity` → `@Table(name = "seleccion_unica")` (a confirmar), `@OneToMany(cascade = CascadeType.ALL) @JoinColumn(name = "id_seleccion_unica") List<OpcionEntity> listaDeOpciones`.
- **Mapper:** `OpcionMapper` (`toDomain`/`toEntity`), `SeleccionUnicaMapper` (`toDomain`/`toEntity`, delega en `OpcionMapper` para la lista, copia campo a campo explícita, sin MapStruct).
- **Repository:** `SeleccionUnicaJpaRepository extends BaseContentRepositorio<SeleccionUnicaEntity>`. `OpcionEntity` no necesita repository propio — se persiste en cascada a través de `SeleccionUnicaEntity`.
- **Adapter:** `SeleccionUnicaJpaAdapter implements SeleccionUnicaRepositoryPort`, delega en `SeleccionUnicaJpaRepository` + `SeleccionUnicaMapper`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. **Confirmado contra el DDL real** provisto por el usuario: `seleccion_unica` (única columna propia: `id`, FK a `pregunta`) y `opcion` (`id`, `la_respuesta_es`, `opcion`, `id_seleccion_unica`, más `id_multiple_opcion` e `id_desplegable_independiente` — columnas de los otros dos tipos que comparten la tabla y que no se tocan en este spec).

---

## Plan de implementación

1. **Nombres de tabla confirmados** contra el DDL real (`seleccion_unica`, `opcion`) — ver "Modelo de datos".
2. **Domain puro.** Crear `content/domain/Opcion.java` y `content/domain/SeleccionUnica.java`.
3. **Entities JPA nuevas.** Crear `OpcionEntity` y `SeleccionUnicaEntity`. Levantar la app y confirmar que arranca sin error de mapeo.
4. **Mappers.** Crear `OpcionMapper` y `SeleccionUnicaMapper`.
5. **Puerto y adapter.** Definir `SeleccionUnicaRepositoryPort` y `SeleccionUnicaJpaAdapter`, apoyado en `SeleccionUnicaJpaRepository`.
6. **Puertos in y servicios de escritura.** Implementar `CrearSeleccionUnicaUseCase`/`Service`, `EditarSeleccionUnicaUseCase`/`Service` (reemplazo completo de opciones), `EliminarSeleccionUnicaUseCase`/`Service`.
7. **Test de paridad.** Crear `SeleccionUnicaParidadTest`: compara `PreguntaService.createaQuestion`/`updateQuestion` contra los nuevos use cases, incluyendo verificación de que la lista de opciones (contenido: texto + `laRespuestaEs`) persiste/actualiza de forma equivalente y que `intentosParaQueDejeDeSerCriticoDisponible` se preserva al editar.
8. **Cablear `PreguntaController`.** Extender las ramas condicionales de `POST /questions` y `PUT /questions` para evaluar también `SELECCION_UNICA`. `DELETE /questions/{id}` no se toca.
9. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: crear y editar una `SeleccionUnica` (con sus opciones), confirmar que responder (`verifyResponse`) y borrar siguen funcionando igual que antes.

---

## Criterios de aceptación

- [x] Existen y compilan `content/domain/{Opcion, SeleccionUnica}.java` sin ninguna anotación de Spring/JPA/Jackson.
- [x] Existen `OpcionEntity` (`@Table(name = "opcion")`) y `SeleccionUnicaEntity` (`@Table(name = "seleccion_unica")`, extiende `PreguntaEntity`), con nombres de tabla confirmados contra la BD real.
- [ ] Al levantar la app con las entities nuevas, no se crean tablas ni columnas nuevas en Postgres (`ddl-auto=validate` arranca sin error de mapeo). *(pendiente: lo confirma el usuario al levantar la app)*
- [x] `POST /questions` con `tipo == SELECCION_UNICA` persiste la pregunta y su lista de opciones (texto + `laRespuestaEs`), produciendo un resultado equivalente al flujo viejo. *(cubierto por `SeleccionUnicaParidadTest`, pendiente de correr)*
- [x] `PUT /questions` con `tipo == SELECCION_UNICA` reemplaza completamente la lista de opciones existentes por la del DTO de edición, preservando `intentosParaQueDejeDeSerCriticoDisponible`. *(cubierto por `SeleccionUnicaParidadTest`, pendiente de correr)*
- [x] `DELETE /questions/{id}` para `SeleccionUnica` sigue funcionando sin cambios, por el camino viejo. *(no tocado — `PreguntaController.delete` sigue llamando a `preguntaService.delete`)*
- [x] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con los demás tipos siguen funcionando sin cambios. *(las ramas nuevas son adicionales — `else`/último `if` sigue delegando a `preguntaService` igual que antes)*
- [x] `verifyResponse` para `SeleccionUnica` sigue funcionando sin cambios (no tocado por este spec).
- [x] Existe `SeleccionUnicaParidadTest`, con al menos un test de paridad para creación, edición (verificando reemplazo de opciones) y borrado.
- [ ] `./mvnw test` corre completo y pasa — **verificación manual del usuario, no ejecutada por el agente.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT`: crear, editar, responder y borrar una `SeleccionUnica` funcionan sin errores visibles. **(pendiente, a cargo del usuario)**
- [x] Ningún import ni endpoint del slice `answering/` fue tocado.
- [x] Las interfaces de `PreguntaSimple` y `VerdaderoOFalso` no fueron modificadas.

---

## Decisiones tomadas y descartadas

- **`content.domain.Opcion` es una clase nueva, no reutiliza la `Opcion` JPA vieja.**
  Descartado: reutilizar `model.AResponder.TiposDePreguntas.Opcion` directamente dentro del domain nuevo. Justificación: esa clase es `@Entity` (anotaciones JPA/Jackson), violaría la regla de `domain/` ("Java puro") que ya se sostuvo en los specs 01 y 02 para `TipoAResponder` (ahí se reutilizó porque es un enum plano sin anotaciones de framework — no es el mismo caso). El DTO de wire (`PostPreguntaDTO`) sigue usando la `Opcion` vieja como tipo de campo porque es un contrato externo que no se toca; la conversión a `content.domain.Opcion` ocurre dentro de los servicios nuevos.

- **Edición de opciones: reemplazo completo, no merge por id.**
  Confirmado con el usuario. Descartado: preservar ids de opciones existentes hciendo upsert. Justificación: replica el comportamiento real del flujo viejo (colección `List` sin `@OrderColumn` = semántica de *bag* en Hibernate, que borra y reinserta todo el contenido en cada `save()`), y evita construir lógica de merge que el sistema actual no tiene.

- **Validación de "debe existir exactamente una opción correcta" (`existeUnaOpcionVerdaderaUnicamente`) no se porta al domain nuevo.**
  Descartado: agregar esa validación en `CrearSeleccionUnicaService`/`EditarSeleccionUnicaService`. Justificación: en el código actual, esa validación ocurre únicamente al responder (`laRespuestaEsCorrecta` → `validacionDeDatosDTO`), no al crear/editar — `FabricaDePreguntas.fromJSON` no la invoca. Portarla ahora sería agregar comportamiento nuevo que el sistema no tiene hoy, fuera del alcance de "migración sin cambiar comportamiento".

- **`OpcionMultiple` queda fuera de este spec** aunque comparta la tabla `opcion` y el patrón `IPreguntaVariasOpciones`.
  Descartado: migrar ambos tipos juntos por compartir código. Justificación: mismo criterio que specs anteriores — un tipo a la vez para acotar el blast radius; `OpcionEntity` nueva puede reutilizarse en el spec de `OpcionMultiple` sin cambios (mismo mapeo unidireccional a tabla `opcion`, solo cambia la columna de join).

- **Interfaces de use case dedicadas por tipo**, siguiendo el criterio ya establecido en el spec 02.

- **`DELETE /questions/{id}` queda 100% en el camino viejo**, mismo razonamiento que specs 01/02.

- **Se agregó `@Getter` (Lombok) a la `Opcion` JPA vieja** (`model.AResponder.TiposDePreguntas.Opcion`), descubierto durante la implementación del paso 6.
  Motivo: el campo `opcion` (texto) es package-private y solo tenía `@Setter` a nivel de clase — sin getter no hay forma de leerlo desde `content.application.service` (paquete distinto) para convertirlo a `content.domain.Opcion`. Se agregó `@Getter` a nivel de clase; Lombok no regenera `getId()` porque ya existe un override manual con ese nombre, así que el único efecto observable es la aparición de `getOpcion()` y `getLaRespuestaEs()` como métodos nuevos — ningún comportamiento existente cambia.
  Descartado: leer el campo por reflexión, o duplicar la clase en el paquete viejo. Justificación: agregar un getter es el cambio más chico y transparente posible; reflexión hubiera sido más frágil para un caso tan simple.

- **`SeleccionUnicaEntity.listaDeOpciones` es `FetchType.EAGER`, a diferencia del `FetchType.LAZY` del modelo viejo.**
  Descubierto durante la implementación del paso 6, al escribir `SeleccionUnicaParidadTest`: este repository nuevo solo lo usan los use cases de escritura (`Crear`/`EditarSeleccionUnicaService`), que siempre necesitan la lista completa disponible dentro de la misma transacción — nunca se usa para listados masivos donde LAZY evitaría N+1. Los endpoints de lectura (`fetch`/`fetch-full`) siguen sirviéndose del modelo viejo con su propio `@EntityGraph`, sin cambios. Cambiar a EAGER en la entity nueva no tiene efecto observable en ningún endpoint existente.
  Descartado: mantener LAZY y envolver las verificaciones del test en una transacción explícita. Justificación: hubiera exigido reestructurar la limpieza de datos del test (que depende de que `@AfterEach` corra fuera de cualquier transacción de prueba) sin ganar nada a cambio, dado que este repository no tiene ningún consumidor que se beneficie de carga perezosa.

---

## Riesgos identificados

- **Bag semantics de Hibernate en la colección `Opcion`.** Al no tener `@OrderColumn` ni índice, cualquier cambio en la lista provoca un delete-and-reinsert completo de las filas hijas en cada `save()`. Esto ya es el comportamiento del flujo viejo, pero hay que replicarlo fielmente en el nuevo mapeo (mismo `cascade = CascadeType.ALL`, sin agregar `orphanRemoval` que no estaba en el original si eso cambiara el comportamiento observable — a verificar en el paso 3).
  *Mitigación:* test de paridad del paso 7 debe cubrir explícitamente un ciclo de edición que cambie el contenido de la lista (agregar/quitar/modificar una opción), no solo creación.

- **Conversión manual `Opcion` (vieja, del DTO) → `content.domain.Opcion` (nueva).** Es lógica nueva que no existe en el flujo viejo (que persiste la lista de `Opcion` del DTO directamente vía `BeanUtils.copyProperties` + cascada JPA). Un mapeo incompleto de campos (`opcion`, `laRespuestaEs`) podría producir un resultado sutilmente distinto sin que sea obvio en revisión de código.
  *Mitigación:* cubierto por el test de paridad del paso 7, comparando contenido de opciones (texto + `laRespuestaEs`) entre ambos caminos.

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas `seleccion_unica`/`opcion`**, mismo riesgo ya identificado en specs 01/02.
  *Mitigación:* mismo criterio — test de paridad corre contra estado limpio, no contra datos ya tocados por el otro camino.
