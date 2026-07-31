# Spec 06 — Migración hexagonal: CRUD de DesplegableIndependiente (sexto y último tipo de pregunta)

**Estado:** Implementado
**Dependencias:** Spec 01 (hexagonal-content-piloto) — reutiliza `content.domain.Pregunta`, `PreguntaEntity`, `BaseContentRepositorio<T>`; Spec 03 (hexagonal-seleccion-unica) — reutiliza `content.domain.Opcion`, `OpcionEntity`, `OpcionMapper` tal cual para el nivel más interno; mismo patrón general validado en specs 02-05, pero introduce un nivel adicional de anidamiento (`DesplegableIndependiente` → `SeleccionUnicaParaDesplegableIndependiente` → `Opcion`), no visto en tipos anteriores.
**Fecha:** 2026-07-30
**Objetivo:** Migrar la creación y edición de `DesplegableIndependiente` —el sexto y último tipo de pregunta pendiente— a arquitectura hexagonal, con una estructura de dos niveles de colección hija: cada `SeleccionUnicaParaDesplegableIndependiente` trae su propia lista de `Opcion` (reutilizada tal cual de spec 03).

---

## Alcance

### Incluido

- **Domain puro:**
  - `content.domain.SeleccionUnicaParaDesplegableIndependiente` — nuevo, Java puro (`id: Long`, `titulo: String`, `listaDeOpciones: List<Opcion>`, reutilizando `content.domain.Opcion` de spec 03 sin cambios).
  - `content.domain.DesplegableIndependiente extends Pregunta` — agrega `listaDeOpciones: List<SeleccionUnicaParaDesplegableIndependiente>`. Sin comportamiento (mismo criterio "anémico" que los tipos anteriores): no se porta el `@PostLoad` de dedupe (`.distinct()`) del modelo viejo, ni `validacionDeDatosDTO` (ya no-op), ni `listaDeOpciones()`/`listaDeOpcionesConLaRespuestaDelUsuario(...)` de la interfaz vieja `IPreguntaVariasOpciones` — todo eso es comportamiento de lectura/respuesta, fuera de alcance.
  - El nombre de campo es `listaDeOpciones` en **ambos** niveles (no `listaDeOpcionDesplegableIndependiente` ni `listaDeOpcionesDisponible`, los nombres del modelo viejo) — mismo criterio de naming que specs 02-05 para mantener simetría entre tipos con colección hija.
- **Infraestructura de persistencia:**
  - `SeleccionUnicaParaDesplegableIndependienteEntity` — nueva, `@Entity @Table(name = "seleccion_unica_para_desplegable_independiente")`, campos `id`/`titulo`, `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_desplegable_independiente") List<OpcionEntity> listaDeOpciones` — reutiliza `OpcionEntity` tal cual (spec 03), columna de join confirmada contra el DDL real.
  - `DesplegableIndependienteEntity extends PreguntaEntity` → `@Table(name = "desplegable_independiente")`, con `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_pregunta_desplegable_ind") List<SeleccionUnicaParaDesplegableIndependienteEntity> listaDeOpciones` — columna de join confirmada contra el DDL real. Mismo criterio EAGER que specs 03-05, en **ambos** niveles: este repository solo lo usan los use cases de escritura, que necesitan el grafo completo dentro de la transacción.
  - `SeleccionUnicaParaDesplegableIndependienteMapper` (`toDomain`/`toEntity`, delega en el `OpcionMapper` ya existente para su lista interna), `DesplegableIndependienteMapper` (`toDomain`/`toEntity`, delega en `SeleccionUnicaParaDesplegableIndependienteMapper` para su lista, copia campo a campo explícita, sin MapStruct) — mismo patrón que los mappers anteriores.
  - `DesplegableIndependienteJpaRepository extends BaseContentRepositorio<DesplegableIndependienteEntity>`. Ni `SeleccionUnicaParaDesplegableIndependienteEntity` ni `OpcionEntity` necesitan repository propio — se persisten en cascada.
  - `DesplegableIndependienteJpaAdapter implements DesplegableIndependienteRepositoryPort`.
  - **No se toca `OpcionEntity` ni `OpcionMapper`** — se reutilizan tal cual, sin ningún cambio.
- **Application — puertos dedicados por tipo** (mismo criterio que specs 02-05: no generalizar):
  - Puerto out: `DesplegableIndependienteRepositoryPort` (`save`, `findById`, `deleteById`).
  - Puertos in: `CrearDesplegableIndependienteUseCase`, `EditarDesplegableIndependienteUseCase`, `EliminarDesplegableIndependienteUseCase`.
  - Servicios: `CrearDesplegableIndependienteService`, `EditarDesplegableIndependienteService`, `EliminarDesplegableIndependienteService` — `idDuenio` seteado directo (sin `Temario.agregarALaLista`), `intentosParaQueDejeDeSerCriticoDisponible` inicializado en `0` al crear y preservado al editar. Conversión manual `List<model...SeleccionUnicaParaDesplegableIndependiente>` (del DTO, campo de wire `listaDeOpcionDesplegableIndependiente` de `PostPreguntaDTO`, tipado directo como la entity vieja) → `List<content.domain.SeleccionUnicaParaDesplegableIndependiente>`, incluyendo la conversión anidada de la lista interna de `Opcion` de cada sub-elemento.
  - **Edición: reemplazo completo en ambos niveles** (se borran y reinsertan todas las `SeleccionUnicaParaDesplegableIndependiente` junto con sus `Opcion`) — mismo criterio de *bag semantics* que specs 03-05, extendido al nivel anidado.
- **Dispatch en `PreguntaController`:** las ramas condicionales de `POST /questions` y `PUT /questions` se extienden para evaluar también `DESPLEGABLE_INDEPENDIENTE` y delegar a los nuevos use cases. `DELETE /questions/{id}` no se toca.
- **`EliminarDesplegableIndependienteUseCase`/`Service`** se implementan igual (cubiertos por test), pero el controller no los invoca — mismo tratamiento que los tipos anteriores.
- **Corrección de bug preexistente:** `DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta` (código viejo, `Repository/PreguntaRepository/`) tiene un typo en `@EntityGraph(attributePaths = {"listaDeOp cionDesplegableIndependiente.listaDeOpcionesDisponible"})` — espacio en medio del nombre del atributo, no coincide con el atributo real (`listaDeOpcionDesplegableIndependiente`). Se corrige a `"listaDeOpcionDesplegableIndependiente.listaDeOpcionesDisponible"`, necesario para que el camino viejo (usado como baseline del test de paridad) funcione correctamente. Mismo criterio que el bug corregido en spec 05.
- **Tests:** `DesplegableIndependienteParidadTest` (integración, mismo patrón que specs 03-05) comparando create/edit contra `PreguntaService.createaQuestion`/`updateQuestion`, incluyendo verificación de la estructura anidada de dos niveles (sub-preguntas + sus opciones), un ciclo de edición que cambie contenido en ambos niveles, y que `intentosParaQueDejeDeSerCriticoDisponible` se preserve.

### Explícitamente NO incluido

- `DELETE /questions/{id}` para `DesplegableIndependiente` — permanece en el camino viejo.
- `verifyResponse`, `laRespuestaEsCorrecta`, `validacionDeDatosDTO` y la lógica de críticos — quedan intactos en el código viejo; pertenecen al slice `answering/`, todavía no migrado.
- El `@PostLoad` de dedupe (`.distinct()`) del modelo viejo `DesplegableIndependiente.init()` — comportamiento de lectura, no se porta al domain nuevo.
- `model...RespuestaDeDesplegableIndependiente` — clase marcada en el propio código como posible código muerto (`//ES POSIBLE ELIMINAR ESTA CLASE?`); no se toca ni se elimina en este spec, fuera de alcance.
- Cambios en `TipoDeTemario`, `AsignadorDeTipoALasPreguntas` o `FabricaDePreguntas` viejos — ya tienen registrado `DesplegableIndependiente`, no requieren cambios.
- Cualquier cambio de esquema de base de datos — se reutilizan `desplegable_independiente`, `seleccion_unica_para_desplegable_independiente` y `opcion` sin modificarlas, confirmado contra el DDL real.
- Mover `TemarioController`/`PreguntaController` a `content/infrastructure/controller/` — diferido al spec final de "relocation".
- Cambios en `AQ-SIMPLE-FRONT`.
- Merge por id de las listas al editar — reemplazo completo en ambos niveles, mismo criterio que specs 03-05.
- Cualquier otra corrección en `DesplegableIndependienteRepository` (código viejo) más allá del typo puntual corregido.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.model.TipoAResponder` y `PostPreguntaDTO` (ya trae `listaDeOpcionDesplegableIndependiente: List<SeleccionUnicaParaDesplegableIndependiente>`, tipado directo como la entity vieja — mismo campo de wire que ya usa el flujo viejo) — mismo contrato, sin tocar.
- `content.domain.Pregunta`, `content.infrastructure.persistence.entity.PreguntaEntity`, `BaseContentRepositorio<T>` (creados en spec 01) — `DesplegableIndependiente`/`DesplegableIndependienteEntity` los extienden igual que los tipos anteriores.
- `content.domain.Opcion`, `content.infrastructure.persistence.entity.OpcionEntity`, `content.infrastructure.persistence.mapper.OpcionMapper` (creados en spec 03) — reutilizados sin ningún cambio para la lista interna de cada `SeleccionUnicaParaDesplegableIndependiente`.

### `content/domain/` (nuevo)

- **`SeleccionUnicaParaDesplegableIndependiente`** — Java puro: `id: Long`, `titulo: String`, `listaDeOpciones: List<Opcion>` (usando el `content.domain.Opcion` ya existente).
- **`DesplegableIndependiente extends Pregunta`** — agrega `listaDeOpciones: List<SeleccionUnicaParaDesplegableIndependiente>`. Sin comportamiento.

### `content/application/` (nuevo)

- **Puerto out:** `DesplegableIndependienteRepositoryPort` (`save`, `findById`, `deleteById`).
- **Puertos in:** `CrearDesplegableIndependienteUseCase`, `EditarDesplegableIndependienteUseCase`, `EliminarDesplegableIndependienteUseCase`.
- **Servicios:** `CrearDesplegableIndependienteService`, `EditarDesplegableIndependienteService`, `EliminarDesplegableIndependienteService`.

### `content/infrastructure/persistence/` (nuevo)

- **Entity:** `SeleccionUnicaParaDesplegableIndependienteEntity` → `@Entity @Table(name = "seleccion_unica_para_desplegable_independiente")`, campos `id`/`titulo`, `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_desplegable_independiente") List<OpcionEntity> listaDeOpciones`.
- **Entity:** `DesplegableIndependienteEntity extends PreguntaEntity` → `@Table(name = "desplegable_independiente")`, `@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) @JoinColumn(name = "id_pregunta_desplegable_ind") List<SeleccionUnicaParaDesplegableIndependienteEntity> listaDeOpciones`.
- **Mapper:** `SeleccionUnicaParaDesplegableIndependienteMapper` (`toDomain`/`toEntity`, delega en `OpcionMapper` para su lista interna), `DesplegableIndependienteMapper` (`toDomain`/`toEntity`, delega en `SeleccionUnicaParaDesplegableIndependienteMapper` para su lista).
- **Repository:** `DesplegableIndependienteJpaRepository extends BaseContentRepositorio<DesplegableIndependienteEntity>`.
- **Adapter:** `DesplegableIndependienteJpaAdapter implements DesplegableIndependienteRepositoryPort`, delega en `DesplegableIndependienteJpaRepository` + `DesplegableIndependienteMapper`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva. Confirmado contra el DDL real provisto por el usuario:

- `desplegable_independiente` (única columna propia: `id`, FK a `pregunta`).
- `seleccion_unica_para_desplegable_independiente` (`id` bigserial, `titulo` varchar(255), `id_pregunta_desplegable_ind` bigint FK a `desplegable_independiente`).
- `opcion` (`id`, `opcion` varchar(255), `la_respuesta_es` boolean, más las columnas de join alternativas `id_seleccion_unica`, `id_multiple_opcion` e `id_desplegable_independiente` — esta última bigint FK a `seleccion_unica_para_desplegable_independiente`, no a `desplegable_independiente` directamente, confirmando el anidado a dos niveles).

### Corrección de bug puntual (código viejo, fuera de `content/`)

- `Repository/PreguntaRepository/DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta`: `@EntityGraph(attributePaths = {"listaDeOp cionDesplegableIndependiente.listaDeOpcionesDisponible"})` → `@EntityGraph(attributePaths = {"listaDeOpcionDesplegableIndependiente.listaDeOpcionesDisponible"})`.
- **(Descubierto durante el Paso 8, no anticipado en la redacción original de esta spec)** `model...DesplegableIndependiente.init()` (`@PostLoad`): `listaDeOpcionDesplegableIndependiente = listaDeOpcionDesplegableIndependiente.stream().distinct().toList()` → `.collect(Collectors.toCollection(ArrayList::new))`. `Stream.toList()` (Java 16+) devuelve una lista inmutable; al recargar la entity vieja y luego guardarla (`PreguntaRepository.save(...)`, patrón usado por `DesplegableIndependienteParidadTest` para preparar el estado de `intentosParaQueDejeDeSerCriticoDisponible` antes de editar, igual que en specs 03-05), Hibernate intenta reemplazar los elementos de esa colección en el `merge()` y falla con `UnsupportedOperationException` al llamar `.clear()` sobre una lista inmutable. Bug puramente del modelo viejo — no relacionado con el domain/entities/mappers nuevos de esta spec — pero bloqueaba el camino viejo usado como baseline del test de paridad, igual que el typo de `@EntityGraph` de arriba. Confirmado con el usuario durante la implementación.

---

## Plan de implementación

1. **Nombres de tabla/columna confirmados** contra el DDL real (`desplegable_independiente`, `seleccion_unica_para_desplegable_independiente`, columnas de join `id_pregunta_desplegable_ind` e `id_desplegable_independiente`) — ver "Modelo de datos".
2. **Domain puro.** Crear `content/domain/SeleccionUnicaParaDesplegableIndependiente.java` y `content/domain/DesplegableIndependiente.java`. No se conecta a nada todavía.
3. **Entities JPA nuevas.** Crear `SeleccionUnicaParaDesplegableIndependienteEntity` y `DesplegableIndependienteEntity`. Levantar la app y confirmar que arranca sin error de mapeo (`ddl-auto=validate`).
4. **Mappers.** Crear `SeleccionUnicaParaDesplegableIndependienteMapper` y `DesplegableIndependienteMapper` (este último delega en el primero para la lista anidada).
5. **Puerto y adapter.** Definir `DesplegableIndependienteRepositoryPort` y `DesplegableIndependienteJpaAdapter`, apoyado en `DesplegableIndependienteJpaRepository`.
6. **Puertos in y servicios de escritura.** Implementar `CrearDesplegableIndependienteUseCase`/`Service`, `EditarDesplegableIndependienteUseCase`/`Service` (reemplazo completo en ambos niveles de colección), `EliminarDesplegableIndependienteUseCase`/`Service`.
7. **Corregir el bug de typo** en `DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta` (`@EntityGraph` con espacio en el nombre del atributo) — necesario para que el camino viejo, usado como baseline, funcione correctamente en el test de paridad del paso siguiente.
8. **Test de paridad.** Crear `DesplegableIndependienteParidadTest`: compara `PreguntaService.createaQuestion`/`updateQuestion` contra los nuevos use cases, verificando la estructura anidada de dos niveles (sub-preguntas y sus opciones), un ciclo de edición que cambie contenido en ambos niveles, y que `intentosParaQueDejeDeSerCriticoDisponible` se preserve al editar.
9. **Cablear `PreguntaController`.** Extender las ramas condicionales de `POST /questions` y `PUT /questions` para evaluar también `DESPLEGABLE_INDEPENDIENTE`. `DELETE /questions/{id}` no se toca.
10. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: crear y editar una `DesplegableIndependiente` (con sus sub-preguntas y opciones), confirmar que responder (`verifyResponse`) y borrar siguen funcionando igual que antes.

---

## Criterios de aceptación

- [ ] Existen y compilan `content/domain/{SeleccionUnicaParaDesplegableIndependiente, DesplegableIndependiente}.java` sin ninguna anotación de Spring/JPA/Jackson.
- [ ] Existen `SeleccionUnicaParaDesplegableIndependienteEntity` (`@Table(name = "seleccion_unica_para_desplegable_independiente")`) y `DesplegableIndependienteEntity` (`@Table(name = "desplegable_independiente")`, extiende `PreguntaEntity`), con nombres de tabla/columna confirmados contra la BD real (DDL provisto por el usuario).
- [ ] Al levantar la app con las entities nuevas, no se crean tablas ni columnas nuevas en Postgres (`ddl-auto=validate` arranca sin error de mapeo). *(a confirmar por el usuario al levantar la app en el paso 3)*
- [ ] `POST /questions` con `tipo == DESPLEGABLE_INDEPENDIENTE` persiste la pregunta y su estructura anidada de dos niveles (sub-preguntas con `titulo` + su lista de `Opcion`), produciendo un resultado equivalente al flujo viejo.
- [ ] `PUT /questions` con `tipo == DESPLEGABLE_INDEPENDIENTE` reemplaza completamente la estructura anidada existente (ambos niveles) por la del DTO de edición, preservando `intentosParaQueDejeDeSerCriticoDisponible`.
- [ ] `DELETE /questions/{id}` para `DesplegableIndependiente` sigue funcionando sin cambios, por el camino viejo.
- [ ] `POST /questions`, `PUT /questions` y `DELETE /questions/{id}` con los demás tipos siguen funcionando sin cambios.
- [ ] `verifyResponse` para `DesplegableIndependiente` sigue funcionando sin cambios (no tocado por este spec).
- [ ] El bug de typo en `DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta` (`@EntityGraph`) está corregido y el método funciona por el camino viejo.
- [ ] Existe `DesplegableIndependienteParidadTest`, con al menos un test de paridad para creación, edición (verificando reemplazo en ambos niveles de la estructura anidada) y borrado.
- [ ] `./mvnw test` corre completo y pasa — **verificación manual del usuario, no ejecutada por el agente.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT`: crear, editar, responder y borrar una `DesplegableIndependiente` funcionan sin errores visibles. **(a cargo del usuario)**
- [ ] Ningún import ni endpoint del slice `answering/` fue tocado.
- [ ] Las interfaces/entities/mappers de `PreguntaSimple`, `VerdaderoOFalso`, `SeleccionUnica`, `OpcionMultiple` y `DesplegableCompartido` no fueron modificadas, incluyendo `OpcionEntity`/`OpcionMapper` (reutilizados sin cambios).

---

## Decisiones tomadas y descartadas

- **Estructura de dos niveles: `DesplegableIndependiente` → `List<SeleccionUnicaParaDesplegableIndependiente>` → `List<Opcion>`.**
  Descartado: aplanar la estructura en un solo nivel de domain (por ejemplo, guardando solo la `Opcion` correcta de cada sub-pregunta). Justificación: el modelo viejo necesita conservar la lista completa de opciones disponibles de cada sub-pregunta (no solo la correcta) para poder mostrarlas al usuario al responder; aplanar perdería esa información aunque el domain nuevo sea "anémico" y no la use todavía.

- **`content.domain.SeleccionUnicaParaDesplegableIndependiente` mantiene el nombre del modelo viejo**, en vez de renombrarse a algo más corto.
  Confirmado con el usuario. Justificación: mismo criterio de las specs anteriores de no inventar terminología nueva sin necesidad clara — el nombre viejo ya describe bien el concepto (una selección única embebida dentro del desplegable independiente).

- **La lista interna de opciones usa `List<Opcion>`, no `Set<Opcion>`** (el modelo viejo usa `Set`).
  Confirmado con el usuario. Justificación: consistencia con el resto del domain nuevo (`SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido` ya usan `List`). El `Set` viejo era un workaround para evitar duplicados por un bug de Hibernate en la carga; ese problema se resuelve en el modelo viejo con el `@PostLoad` de `.distinct()`, que de todas formas no se porta al domain nuevo (es comportamiento de lectura).

- **`content.domain.Opcion`/`OpcionEntity`/`OpcionMapper` (creados en spec 03) se reutilizan tal cual para el nivel más interno, sin ningún cambio.**
  Confirmado con el usuario. Mismo criterio que spec 04 con `OpcionMultiple`: la forma de datos es idéntica (texto + `laRespuestaEs`), y la entity ya es agnóstica de qué `@JoinColumn` la referencia.

- **Edición: reemplazo completo en ambos niveles, no merge por id.**
  Confirmado con el usuario. Mismo criterio y misma justificación que specs 03-05: colección `List` sin `@OrderColumn` = semántica de *bag* en Hibernate (borra y reinserta todo el contenido en cada `save()`), extendido naturalmente al nivel anidado.

- **`FetchType.EAGER` en ambos niveles de las entities nuevas**, a diferencia del `LAZY` del modelo viejo.
  Confirmado con el usuario. Mismo motivo que specs 03-05: este repository solo lo usan los use cases de escritura, que necesitan el grafo completo en la misma transacción; los endpoints de lectura siguen sirviéndose del modelo viejo, sin cambios.

- **Se corrige el bug de typo en `DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta`** (espacio en medio de `listaDeOpcionDesplegableIndependiente` dentro de `@EntityGraph`), en vez de dejarlo intacto.
  Confirmado con el usuario. Mismo criterio que los bugs corregidos en spec 05: el camino viejo se usa como baseline del test de paridad, y este typo lo rompe — corregirlo es necesario para poder verificar equivalencia, aunque esté formalmente fuera del alcance de la migración hexagonal en sí.

- **Se corrige también el `@PostLoad init()` de `model...DesplegableIndependiente`** (cambia `.toList()` por `.collect(Collectors.toCollection(ArrayList::new))`), pese a no estar identificado en la redacción original de esta spec.
  Descubierto al correr `DesplegableIndependienteParidadTest` en el Paso 8: el test de edición reproduce el mismo patrón que specs 03-05 (fetch por el repo viejo, mutar `intentosParaQueDejeDeSerCriticoDisponible`, `save()`), pero al recargar la entity vieja el `@PostLoad` la deja con una lista inmutable (`Stream.toList()`), y el `merge()` posterior explota con `UnsupportedOperationException` al intentar `.clear()` esa lista — rompiendo el camino viejo usado como baseline, antes incluso de llegar al código nuevo. Confirmado con el usuario: mismo criterio que el typo de `@EntityGraph` — necesario para que el baseline funcione, aunque sea un bug ajeno a la migración hexagonal en sí. No se toca el `.distinct()` en sí (sigue descartado según la decisión de más abajo), solo el tipo de colección que produce.

- **`model...RespuestaDeDesplegableIndependiente` no se toca ni se evalúa para eliminación**, pese a estar marcada en el propio código como posible código muerto.
  Descartado: limpiar esa clase como parte de este spec. Justificación: mismo criterio que specs anteriores de no tocar código fuera del camino de escritura que se está migrando; si es código muerto, su eliminación merece su propia revisión aislada, no colgarse de esta migración.

- **Interfaces de use case dedicadas por tipo**, siguiendo el criterio ya establecido en specs 02-05.

- **`DELETE /questions/{id}` queda 100% en el camino viejo**, mismo razonamiento que specs 01-05.

- **No se porta el `@PostLoad` de dedupe (`.distinct()`) del modelo viejo.**
  Descartado: replicar la deduplicación en el domain nuevo. Justificación: es un workaround de lectura para un bug de duplicados al traer de la BD (afecta cómo se muestran las sub-preguntas al leer), no de escritura — pertenece al slice `answering/`/lectura, todavía no migrado.

---

## Riesgos identificados

- **Semántica de *bag* de Hibernate en dos niveles simultáneos.** Al reemplazar completamente `DesplegableIndependienteEntity.listaDeOpciones`, Hibernate debe borrar y reinsertar tanto las filas de `seleccion_unica_para_desplegable_independiente` como, en cascada, las de `opcion` referenciadas por cada una. Un mapeo incompleto en el nivel intermedio (por ejemplo, no reasignar `SeleccionUnicaParaDesplegableIndependienteEntity.listaDeOpciones` al construir la entity nueva en el mapper) podría dejar sub-preguntas sin sus opciones, o disparar una excepción de Hibernate al hacer commit.
  *Mitigación:* el test de paridad del paso 8 debe verificar explícitamente el contenido de ambos niveles después de un ciclo de edición completo, no solo el nivel superior.

- **Columna de join `id_desplegable_independiente` en la tabla `opcion` referencia a `seleccion_unica_para_desplegable_independiente`, no a `desplegable_independiente`.** El nombre de la columna es potencialmente engañoso (sugiere una relación directa con `desplegable_independiente`, cuando en realidad apunta al nivel intermedio) — mismo tipo de riesgo ya identificado en spec 05 con `id_pregunta`. Un copy-paste apresurado desde `SeleccionUnicaEntity`/`OpcionMultipleEntity` podría mapear mal el `@JoinColumn` de `SeleccionUnicaParaDesplegableIndependienteEntity.listaDeOpciones`.
  *Mitigación:* verificar en el paso 3 que `@JoinColumn(name = "id_desplegable_independiente")` está en `SeleccionUnicaParaDesplegableIndependienteEntity` (no en `DesplegableIndependienteEntity`) y apunta a la columna ya confirmada contra el DDL real.

- **Conversión manual anidada a dos niveles** (`model...SeleccionUnicaParaDesplegableIndependiente` → `content.domain.SeleccionUnicaParaDesplegableIndependiente`, incluyendo su lista interna de `Opcion`). Mismo riesgo ya identificado en specs 03-05 pero agravado por el nivel extra: un mapeo incompleto de campos en cualquiera de los dos niveles podría producir un resultado sutilmente distinto sin ser obvio en revisión de código.
  *Mitigación:* cubierto por el test de paridad del paso 8, comparando contenido completo (títulos de sub-preguntas + texto/`laRespuestaEs` de cada opción) entre ambos caminos.

- **Tabla `opcion` ahora compartida entre tres columnas de join distintas** (`id_seleccion_unica`, `id_multiple_opcion`, `id_desplegable_independiente`), con `DesplegableIndependiente` como tercer consumidor vía un nivel intermedio. Mismo riesgo ya identificado en specs 04/05: un mapeo incorrecto de columna de join podría, en el peor caso, hacer que Hibernate reasigne o pise filas de `Opcion` que pertenecen a otro tipo de pregunta.
  *Mitigación:* mismo criterio — test de paridad corre contra estado limpio, y se verifica que `DesplegableIndependienteJpaRepository` no comparte ningún query cruzado con `SeleccionUnicaJpaRepository`/`OpcionMultipleJpaRepository`.

- **Corrección del bug de typo en `DesplegableIndependienteRepository.findByIdWithTeoriaDeLaPregunta` podría tener efectos colaterales no anticipados** en el camino viejo, si algún otro código dependía (aunque sea accidentalmente) del comportamiento roto del `@EntityGraph`.
  *Mitigación:* el cambio es acotado a un solo `attributePaths`; `./mvnw test` completo (paso 10, a cargo del usuario) cubre el resto del camino viejo para los demás tipos.

- **Coexistencia de dos jerarquías JPA sobre las mismas tablas `desplegable_independiente`/`seleccion_unica_para_desplegable_independiente`/`opcion`**, mismo riesgo ya identificado en specs 01-05.
  *Mitigación:* mismo criterio — test de paridad corre contra estado limpio, no contra datos ya tocados por el otro camino.
