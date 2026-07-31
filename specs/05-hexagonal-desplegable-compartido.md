# Spec 05 — Migración hexagonal: CRUD de DesplegableCompartido (quinto tipo de pregunta)

**Estado:** Implementado
**Dependencias:** Spec 01 (hexagonal-content-piloto) — reutiliza `content.domain.Pregunta`, `PreguntaEntity`, `BaseContentRepositorio<T>`; Spec 03 (hexagonal-seleccion-unica) y Spec 04 (hexagonal-opcion-multiple) — mismo patrón de colección hija, pero **no reutiliza `content.domain.Opcion`/`OpcionEntity`/`OpcionMapper`**: `DesplegableCompartido` tiene una opción hija con forma distinta (`pregunta`+`respuesta`, no `opcion`+`laRespuestaEs`), así que requiere su propia clase `OpcionDeDesplegableCompartido` en domain/infraestructura.
**Fecha:** 2026-07-30

**Objetivo:** Migrar la creación y edición de `DesplegableCompartido` a arquitectura hexagonal, replicando el patrón validado en `SeleccionUnica`/`OpcionMultiple` (specs 03/04) pero con una clase de opción hija propia (`OpcionDeDesplegableCompartido`, campos `pregunta`/`respuesta`) en lugar de reutilizar `content.domain.Opcion`.

---

## Alcance

### Incluido

- **Domain puro:**
  - `content.domain.OpcionDeDesplegableCompartido` — nuevo, Java puro (`id: Long`, `pregunta: String`, `respuesta: String`). Clase nueva y distinta de `content.domain.Opcion` (que tiene `opcion`/`laRespuestaEs`), porque el modelo viejo `OpcionDeDesplegableCompartido` tiene forma diferente.
  - `content.domain.DesplegableCompartido extends Pregunta` — agrega `listaDeOpciones: List<OpcionDeDesplegableCompartido>`. Sin comportamiento (mismo criterio "anémico" que los tipos anteriores): no se porta `validacionDeDatosDTO` (ya es no-op en el viejo), ni el `@PostLoad` que baraja (`Collections.shuffle`), ni `posiblesRespuestasParaCadaOpcion()` — todo eso es comportamiento de lectura/respuesta, fuera de alcance.
  - El nombre del campo es `listaDeOpciones` (no `listaDeOpcionDesplegableCompartido`, el nombre del modelo viejo) — mismo criterio de naming ya usado en `SeleccionUnica`/`OpcionMultiple` para mantener simetría entre los tipos con colección hija.
- **Infraestructura de persistencia:**
  - `OpcionDeDesplegableCompartidoEntity` — nueva, `@Entity @Table(name = "opcion_de_desplegable_compartido")`, campos `id`/`pregunta`/`respuesta`. Sin relación bidireccional a su padre (unidireccional, igual que el modelo viejo).
  - `DesplegableCompartidoEntity extends PreguntaEntity` → `@Table(name = "desplegable_compartido")`, con `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_pregunta") List<OpcionDeDesplegableCompartidoEntity> listaDeOpciones` — mismo criterio EAGER que `SeleccionUnicaEntity`/`OpcionMultipleEntity` (specs 03/04): este repository solo lo usan los use cases de escritura, que necesitan la lista completa dentro de la transacción. Columna de join confirmada contra el DDL real: `id_pregunta` (FK a `desplegable_compartido`, no a `pregunta` — nombre heredado del modelo viejo pese a lo confuso del nombre).
  - `OpcionDeDesplegableCompartidoMapper` (`toDomain`/`toEntity`), `DesplegableCompartidoMapper` (`toDomain`/`toEntity`, delega en `OpcionDeDesplegableCompartidoMapper` para la lista, copia campo a campo explícita, sin MapStruct) — mismo patrón que `OpcionMapper`/`SeleccionUnicaMapper`.
  - `DesplegableCompartidoJpaRepository extends BaseContentRepositorio<DesplegableCompartidoEntity>`. `OpcionDeDesplegableCompartidoEntity` no necesita repository propio — se persiste en cascada.
  - `DesplegableCompartidoJpaAdapter implements DesplegableCompartidoRepositoryPort`.
- **Application — puertos dedicados por tipo** (mismo criterio que specs 02-04: no generalizar):
  - Puerto out: `DesplegableCompartidoRepositoryPort` (`save`, `findById`, `deleteById`).
  - Puertos in: `CrearDesplegableCompartidoUseCase`, `EditarDesplegableCompartidoUseCase`, `EliminarDesplegableCompartidoUseCase`.
  - Servicios: `CrearDesplegableCompartidoService`, `EditarDesplegableCompartidoService`, `EliminarDesplegableCompartidoService` — mismo patrón que `OpcionMultiple`: `idDuenio` seteado directo (sin `Temario.agregarALaLista`), `intentosParaQueDejeDeSerCriticoDisponible` inicializado en `0` al crear y preservado al editar. Conversión manual `List<model...DesplegableCompartido.OpcionDeDesplegableCompartido>` (del DTO, campo de wire `listaDeOpcionDesplegableCompartido` de `PostPreguntaDTO`) → `List<content.domain.OpcionDeDesplegableCompartido>`, campo a campo (`pregunta`, `respuesta` ← `getRespuestaCorrecta()`).
  - **Edición: reemplazo completo de la lista de opciones**, igual que `SeleccionUnica`/`OpcionMultiple` — misma semántica de *bag* de Hibernate.
- **Dispatch en `PreguntaController`:** las ramas condicionales de `POST /questions` y `PUT /questions` se extienden para evaluar también `DESPLEGABLE_COMPARTIDO` y delegar a los nuevos use cases. `DELETE /questions/{id}` no se toca.
- **`EliminarDesplegableCompartidoUseCase`/`Service`** se implementan igual (cubiertos por test), pero el controller no los invoca — mismo tratamiento que los tipos anteriores.
- **Tests:** `DesplegableCompartidoParidadTest` (integración, mismo patrón que `OpcionMultipleParidadTest`) comparando create/edit contra `PreguntaService.createaQuestion`/`updateQuestion`, incluyendo que `intentosParaQueDejeDeSerCriticoDisponible` se preserve y un ciclo de edición que cambie el contenido de la lista de opciones.

### Explícitamente NO incluido

- `DELETE /questions/{id}` para `DesplegableCompartido` — permanece en el camino viejo.
- `verifyResponse`, `laRespuestaEsCorrecta`, `validacionDeDatosDTO`, `posiblesRespuestasParaCadaOpcion()` y la lógica de críticos — quedan intactos en el código viejo; pertenecen al slice `answering/`, todavía no migrado.
- El `@PostLoad` de `Collections.shuffle(listaDeOpcionDesplegableCompartido)` del modelo viejo — comportamiento de lectura, no se porta al domain nuevo.
- `DesplegableIndependiente` — el sexto y último tipo, se migra en un spec posterior, mismo criterio de "uno a la vez".
- Cambios en `TipoDeTemario`, `AsignadorDeTipoALasPreguntas` o `FabricaDePreguntas` viejos.
- Cualquier cambio de esquema de base de datos — se reutilizan `desplegable_compartido` y `opcion_de_desplegable_compartido` sin modificarlas (confirmado contra el DDL real).
- Mover `TemarioController`/`PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Merge por id de la lista de opciones al editar — reemplazo completo, mismo criterio que specs 03/04.
- Reutilizar `content.domain.Opcion`/`OpcionEntity`/`OpcionMapper` para este tipo — tienen forma distinta, se descarta explícitamente (ver "Decisiones").

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` y `PostPreguntaDTO` (ya trae `listaDeOpcionDesplegableCompartido: List<OpcionDeDesplegableCompartido>`, el mismo campo de wire que usa el flujo viejo) — mismo contrato, sin tocar.
- `content.domain.Pregunta`, `content.infrastructure.persistence.entity.PreguntaEntity`, `BaseContentRepositorio<T>` (creados en spec 01) — `DesplegableCompartido`/`DesplegableCompartidoEntity` los extienden igual que los tipos anteriores.

### `content/domain/` (nuevo)

- **`OpcionDeDesplegableCompartido`** — Java puro: `id: Long`, `pregunta: String`, `respuesta: String`.
- **`DesplegableCompartido extends Pregunta`** — agrega `listaDeOpciones: List<OpcionDeDesplegableCompartido>`. Sin comportamiento.

### `content/application/` (nuevo)

- **Puerto out:** `DesplegableCompartidoRepositoryPort` (`save`, `findById`, `deleteById`).
- **Puertos in:** `CrearDesplegableCompartidoUseCase`, `EditarDesplegableCompartidoUseCase`, `EliminarDesplegableCompartidoUseCase`.
- **Servicios:** `CrearDesplegableCompartidoService`, `EditarDesplegableCompartidoService`, `EliminarDesplegableCompartidoService`.

### `content/infrastructure/persistence/` (nuevo)

- **Entity:** `OpcionDeDesplegableCompartidoEntity` → `@Entity @Table(name = "opcion_de_desplegable_compartido")`, campos `id`/`pregunta`/`respuesta`.
- **Entity:** `DesplegableCompartidoEntity extends PreguntaEntity` → `@Table(name = "desplegable_compartido")`, `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_pregunta") List<OpcionDeDesplegableCompartidoEntity> listaDeOpciones`.
- **Mapper:** `OpcionDeDesplegableCompartidoMapper` (`toDomain`/`toEntity`), `DesplegableCompartidoMapper` (`toDomain`/`toEntity`, delega en `OpcionDeDesplegableCompartidoMapper` para la lista).
- **Repository:** `DesplegableCompartidoJpaRepository extends BaseContentRepositorio<DesplegableCompartidoEntity>`.
- **Adapter:** `DesplegableCompartidoJpaAdapter implements DesplegableCompartidoRepositoryPort`, delega en `DesplegableCompartidoJpaRepository` + `DesplegableCompartidoMapper`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. **Confirmado contra el DDL real** provisto por el usuario:
- `desplegable_compartido` (única columna propia: `id`, FK a `pregunta`).
- `opcion_de_desplegable_compartido` (`id` bigserial, `pregunta` varchar(255), `respuesta` varchar(255), `id_pregunta` bigint FK a `desplegable_compartido`).

---

## Plan de implementación

1. **Nombres de tabla/columna confirmados** contra el DDL real (`desplegable_compartido`, `opcion_de_desplegable_compartido`, columna de join `id_pregunta`) — ver "Modelo de datos".
2. **Domain puro.** Crear `content/domain/OpcionDeDesplegableCompartido.java` y `content/domain/DesplegableCompartido.java`. No se conecta a nada todavía.
3. **Entities JPA nuevas.** Crear `OpcionDeDesplegableCompartidoEntity` y `DesplegableCompartidoEntity`. Levantar la app y confirmar que arranca sin error de mapeo (`ddl-auto=validate`).
4. **Mappers.** Crear `OpcionDeDesplegableCompartidoMapper` y `DesplegableCompartidoMapper`.
5. **Puerto y adapter.** Definir `DesplegableCompartidoRepositoryPort` y `DesplegableCompartidoJpaAdapter`, apoyado en `DesplegableCompartidoJpaRepository`.
6. **Puertos in y servicios de escritura.** Implementar `CrearDesplegableCompartidoUseCase`/`Service`, `EditarDesplegableCompartidoUseCase`/`Service` (reemplazo completo de opciones), `EliminarDesplegableCompartidoUseCase`/`Service`.
7. **Test de paridad.** Crear `DesplegableCompartidoParidadTest`: compara `PreguntaService.createaQuestion`/`updateQuestion` contra los nuevos use cases, incluyendo verificación de que la lista de opciones (`pregunta` + `respuesta`) persiste/actualiza de forma equivalente, un ciclo de edición que cambie el contenido de la lista, y que `intentosParaQueDejeDeSerCriticoDisponible` se preserve al editar.
8. **Cablear `PreguntaController`.** Extender las ramas condicionales de `POST /questions` y `PUT /questions` para evaluar también `DESPLEGABLE_COMPARTIDO`. `DELETE /questions/{id}` no se toca.
9. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: crear y editar una `DesplegableCompartido` (con sus opciones), confirmar que responder (`verifyResponse`) y borrar siguen funcionando igual que antes.

---

## Criterios de aceptación

- [ ] Existen y compilan `content/domain/{OpcionDeDesplegableCompartido, DesplegableCompartido}.java` sin ninguna anotación de Spring/JPA/Jackson.
- [ ] Existen `OpcionDeDesplegableCompartidoEntity` (`@Table(name = "opcion_de_desplegable_compartido")`) y `DesplegableCompartidoEntity` (`@Table(name = "desplegable_compartido")`, extiende `PreguntaEntity`), con nombres de tabla/columna confirmados contra la BD real.
- [ ] Al levantar la app con las entities nuevas, no se crean tablas ni columnas nuevas en Postgres (`ddl-auto=validate` arranca sin error de mapeo). *(a confirmar por el usuario al levantar la app en el paso 3)*
- [ ] `POST /questions` con `tipo == DESPLEGABLE_COMPARTIDO` persiste la pregunta y su lista de opciones (`pregunta` + `respuesta`), produciendo un resultado equivalente al flujo viejo.
- [ ] `PUT /questions` con `tipo == DESPLEGABLE_COMPARTIDO` reemplaza completamente la lista de opciones existentes por la del DTO de edición, preservando `intentosParaQueDejeDeSerCriticoDisponible`.
- [ ] `DELETE /questions/{id}` para `DesplegableCompartido` sigue funcionando sin cambios, por el camino viejo.
- [ ] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con los demás tipos siguen funcionando sin cambios.
- [ ] `verifyResponse` para `DesplegableCompartido` sigue funcionando sin cambios (no tocado por este spec).
- [ ] Existe `DesplegableCompartidoParidadTest`, con al menos un test de paridad para creación, edición (verificando reemplazo de opciones) y borrado.
- [ ] `./mvnw test` corre completo y pasa — **verificación manual del usuario, no ejecutada por el agente.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT`: crear, editar, responder y borrar una `DesplegableCompartido` funcionan sin errores visibles. **(a cargo del usuario)**
- [ ] Ningún import ni endpoint del slice `answering/` fue tocado.
- [ ] Las interfaces/entities/mappers de `PreguntaSimple`, `VerdaderoOFalso`, `SeleccionUnica` y `OpcionMultiple` no fueron modificadas.

---

## Decisiones tomadas y descartadas

- **`content.domain.OpcionDeDesplegableCompartido` es una clase nueva, distinta de `content.domain.Opcion`.**
  Descartado: reutilizar `content.domain.Opcion` (de spec 03) ajustando su significado (`opcion`→`pregunta`, `laRespuestaEs`→`respuesta`). Justificación: son formas de datos genuinamente distintas — `Opcion` es booleana (texto + si es la correcta), `OpcionDeDesplegableCompartido` es texto-a-texto (enunciado del hueco + respuesta esperada). Forzar una clase compartida acoplaría dos conceptos de dominio distintos solo por parecido superficial de nombre.

- **El nombre del campo en domain es `listaDeOpciones`, no `listaDeOpcionDesplegableCompartido`** (el nombre del modelo viejo).
  Mismo criterio que specs 03/04: mantiene simetría entre todos los tipos con colección hija, dado que la conversión al wire DTO ya es manual en el servicio.

- **Edición de opciones: reemplazo completo, no merge por id.** Mismo criterio y misma justificación que specs 03/04: colección `List` sin `@OrderColumn` = semántica de *bag* en Hibernate.

- **`DesplegableCompartidoEntity.listaDeOpciones` es `FetchType.EAGER`**, a diferencia del `FetchType.LAZY` del modelo viejo — mismo motivo que specs 03/04: este repository solo lo usan los use cases de escritura, que necesitan la lista completa en la misma transacción.

- **No se replica `orphanRemoval = true`** (presente en el modelo viejo `DesplegableCompartido.listaDeOpcionDesplegableCompartido`) en la entity nueva.
  Descartado: agregar `orphanRemoval = true` a `DesplegableCompartidoEntity.listaDeOpciones` para calcar el mapeo viejo exactamente. Justificación: mismo razonamiento que specs 03/04 con `cascade = CascadeType.ALL` — al reemplazar la lista completa en cada edición (reemplazo completo, no mutación in-place de la colección gestionada), la semántica de *bag* de Hibernate ya borra y reinserta todas las filas hijas en cada `save()`; `orphanRemoval` solo tendría efecto observable si se removieran elementos de la colección gestionada por JPA sin reasignarla, cosa que este flujo de escritura no hace.

- **Interfaces de use case dedicadas por tipo**, siguiendo el criterio ya establecido en specs 02-04.

- **`DELETE /questions/{id}` queda 100% en el camino viejo**, mismo razonamiento que specs anteriores.

- **`DesplegableIndependiente` queda fuera de este spec.**
  Descartado: migrar ambos tipos "desplegable" juntos por parecido de nombre. Justificación: mismo criterio de "un tipo a la vez" para acotar el blast radius — `DesplegableIndependiente` tiene su propia forma de datos (`SeleccionUnicaParaDesplegableIndependiente`, que a su vez tiene su propia lista de `Opcion`), se evalúa en un spec posterior.

---

## Riesgos identificados

- **Columna de join `id_pregunta` con nombre potencialmente confuso.** Es el nombre real en la BD (confirmado contra el DDL), pero es distinto al patrón `id_seleccion_unica`/`id_multiple_opcion` de los tipos anteriores y no referencia literalmente a `pregunta` sino a `desplegable_compartido`. Un mapeo apresurado por copy-paste desde `SeleccionUnicaEntity`/`OpcionMultipleEntity` podría dejar el nombre de columna de esos tipos en vez del correcto.
  *Mitigación:* verificar en el paso 3 que `@JoinColumn(name = "id_pregunta")` apunta a la columna real (ya confirmada contra el DDL) antes de escribir el mapper.

- **Conversión manual `model...OpcionDeDesplegableCompartido` (vieja, del DTO) → `content.domain.OpcionDeDesplegableCompartido` (nueva).** Mismo riesgo ya identificado en specs 03/04: lógica nueva que no existe en el flujo viejo, un mapeo incompleto de campos (`pregunta`, `respuesta`) podría producir un resultado sutilmente distinto sin ser obvio en revisión de código.
  *Mitigación:* cubierto por el test de paridad del paso 7, comparando contenido de opciones (`pregunta` + `respuesta`) entre ambos caminos.

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas `desplegable_compartido`/`opcion_de_desplegable_compartido`**, mismo riesgo ya identificado en specs 01-04.
  *Mitigación:* mismo criterio — test de paridad corre contra estado limpio, no contra datos ya tocados por el otro camino.

- **Bug preexistente encontrado y corregido en `DesplegableCompartidoRepositorio` (código viejo, `Repository/PreguntaRepository/`).** Los métodos `findById`/`findByIdWithTeoriaDeLaPregunta` tenían `@EntityGraph(attributePaths = {"listaDeOpciones"})`, un nombre de atributo que no existe en la entity vieja (`model...DesplegableCompartido.DesplegableCompartido.listaDeOpcionDesplegableCompartido`) — copy-paste desde `OpcionMultipleRepository`/`SeleccionUnicaRepository`, que sí usan `listaDeOpciones`. Esto rompía `PreguntaService.updateQuestion` para `DESPLEGABLE_COMPARTIDO` por el camino viejo (lanzaba `InvalidDataAccessApiUsageException` al ejecutar el test de paridad). Se corrigió a `listaDeOpcionDesplegableCompartido` en ambos métodos, fuera del alcance formal de esta spec pero necesario para que el flujo viejo (usado como baseline de comparación) funcionara.

- **Segundo bug preexistente encontrado y corregido: `orphanRemoval = true` en el modelo viejo `model...DesplegableCompartido.DesplegableCompartido.listaDeOpcionDesplegableCompartido`.** `PreguntaService.updateQuestion` usa `BeanUtils.copyProperties(preguntaDTO, pregunta)`, que reemplaza la referencia completa de la colección gestionada por Hibernate en vez de mutarla in-place. Con `orphanRemoval = true`, esto viola una regla de Hibernate (una colección con `orphanRemoval` no puede ser "dereferenciada" así) y lanzaba `HibernateException` al hacer commit — es decir, **`PUT /questions` para `DesplegableCompartido` estaba roto en producción**, independientemente de esta migración. El modelo viejo `OpcionMultiple` (mismo patrón de `BeanUtils.copyProperties`) no tiene `orphanRemoval = true` en su `@OneToMany`, por eso no sufre este problema. Se quitó `orphanRemoval = true` de la entity vieja para alinearla con `OpcionMultiple`/`SeleccionUnica`, corrigiendo el bug real de producción y desbloqueando el test de paridad. Fuera del alcance formal de esta spec, pero es una corrección de bug preexistente, no un cambio de comportamiento del código nuevo.
