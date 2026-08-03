# Spec 17 — Portar queries nativas de AResponder/Temario legacy al lado hexagonal, incluyendo borrado en cascada

**Estado:** Implementado
**Dependencias:** Spec 16 (deja `Repository/AResponderRepository` y `Repository/TemarioRepository` como la única superficie legacy restante consumida por producción, vía `TemarioJpaAdapter` y `AResponderTipoLookupJpaAdapter`).
**Fecha:** 2026-08-02
**Objetivo:** Eliminar `Repository/AResponderRepository.java` y `Repository/TemarioRepository.java` portando sus dos queries SQL nativas recursivas (`getIssueItems`, `getCriticsIdsForQuestion`) a una nueva interfaz `AResponderJpaRepository` sobre el `AResponderEntity` hexagonal ya existente, y reemplazando el `deleteById` legacy — que hoy depende del cascade de Hibernate en la entidad `Temario` legacy (`@OneToMany(cascade = CascadeType.ALL)`) — por un borrado en cascada explícito dentro de `TemarioJpaAdapter`, construido sobre una tercera query nativa nueva (`findDescendantIds`) y despacho por tipo hacia los 6 repositorios JPA de pregunta.

---

## Alcance

### Incluido

- **Nueva interfaz `AResponderJpaRepository`** en `content/infrastructure/persistence/repository/`, `extends BaseContentRepositorio<AResponderEntity>`, con tres métodos:
  - `getIssueItems(Long id)` → `List<QuestionnaireItem>` — SQL recursivo copiado tal cual desde `Repository/AResponderRepository`.
  - `getCriticsIdsForQuestion(Long id)` → `ArrayList<Long>` — SQL recursivo copiado tal cual desde `Repository/AResponderRepository`.
  - **`findDescendantIds(Long id)` → `List<AResponderIdTipoProjection>` (método nuevo, no existía en el repo legacy)** — variante del mismo CTE recursivo `WITH RECURSIVE TODO_EL_CONTENIDO_DEL_TEMA` ya usado en las otras dos queries, sin el filtro de nivel, devolviendo `(id, tipo)` de **todos** los descendientes de `:id` (excluyéndose a sí mismo).
- **Nueva proyección `AResponderIdTipoProjection`** en `projections/`, con `getId()`/`getType()` — mismo estilo que `QuestionnaireItem`/`IssueOrQuestionnaireProjection` ya existentes.
- **`TemarioJpaAdapter` (modificado):**
  - Se elimina la dependencia a `com.lorenzomar3.AQ.Repository.TemarioRepository temarioRepositoryViejo` y `com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo` (campos + parámetros de constructor).
  - Se agregan como dependencias nuevas los 6 `*JpaRepository` de pregunta (`PreguntaSimpleJpaRepository`, `VerdaderoOFalsoJpaRepository`, `SeleccionUnicaJpaRepository`, `OpcionMultipleJpaRepository`, `DesplegableCompartidoJpaRepository`, `DesplegableIndependienteJpaRepository`) y `AResponderJpaRepository`.
  - `deleteById(Long id)` pasa a: (1) obtener `findDescendantIds(id)`; (2) borrar cada descendiente vía un `Map<TipoAResponder, Consumer<Long>>` construido en `@PostConstruct` (mismo patrón de dispatch ya establecido en specs 09/12/15) que resuelve al `*JpaRepository` correcto según el tipo (`TEMA`/`SUBTEMA` → `temarioJpaRepository`; cada tipo de pregunta → su `*JpaRepository`); (3) borrar el propio nodo `id` vía `temarioJpaRepository.deleteById(id)`. El orden de borrado no es crítico (no hay `ON DELETE CASCADE` a nivel de constraint de DB — verificado que `id_del_duenio` es una columna simple sin FK declarada), pero se borran descendientes antes que el nodo raíz por claridad.
  - `findDirectChildren`, `findIssueItems`, `findCriticalQuestionIds` pasan a usar `AResponderJpaRepository` en vez de `aResponderRepositoryViejo`.
- **`AResponderTipoLookupJpaAdapter` (modificado):** reemplaza `com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo` por `AResponderJpaRepository`; `findTipoById` pasa de `AResponder::getTipo` (legacy) a `AResponderEntity::getTipo` (hexagonal).
- **Borrado de `Repository/AResponderRepository.java` y `Repository/TemarioRepository.java`** — confirmado por grep que, tras los cambios anteriores, no queda ningún caller en `src/main` ni `src/test`.
- **Limpieza de `dto/newDto/CreateQuestionResponseDTO.java`:** se quitan los imports muertos `com.lorenzomar3.AQ.model.AResponder.AResponder` y `com.lorenzomar3.AQ.model.AResponder.Temario.TipoCuestionario`.
- **Tests de integración nuevos** (`@DataJpaTest` + `@AutoConfigureTestDatabase(replace = Replace.NONE)` contra Postgres real):
  - `AResponderJpaRepository`: `getIssueItems`, `getCriticsIdsForQuestion`, `findDescendantIds`.
  - `TemarioJpaAdapter.deleteById`: borrar un tema con hijos de varios tipos (subtema con preguntas de al menos 2 tipos distintos) y verificar que ninguna fila sobrevive en ninguna tabla involucrada — este es el test que reemplaza la red de contención que antes daba el cascade de Hibernate.

### Explícitamente NO incluido

- **`model/AResponder/**` (árbol de entidades completo)** — sigue vivo (Frente B, spec futuro aparte). Ver spec 16 → "Explícitamente NO incluido" para el detalle de por qué.
- **`PostPreguntaDTO`, `RespuestaDePreguntaDTO`, `PreguntaController`** — no se tocan.
- **`projections/QuestionnaireItem.java`, `projections/IssueOrQuestionnaireProjection.java`** — no cambian.
- **`TemarioRepositoryPort`, `AResponderTipoLookupPort`** — no cambian de forma; `deleteById` mantiene exactamente el mismo contrato observable ("borra el nodo y todo su subárbol"), solo cambia la implementación interna del adapter.
- **`EliminarIssueService`** — no se modifica; sigue llamando a `temarioRepositoryPort.deleteById(id)` sin saber nada del recorrido/despacho nuevo.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

```java
// projections/AResponderIdTipoProjection.java
package com.lorenzomar3.AQ.projections;

import com.lorenzomar3.AQ.model.TipoAResponder;

public interface AResponderIdTipoProjection {
    Long getId();
    TipoAResponder getType();
}
```

```java
// content/infrastructure/persistence/repository/AResponderJpaRepository.java
package com.lorenzomar3.AQ.content.infrastructure.persistence.repository;

import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.AResponderEntity;
import com.lorenzomar3.AQ.projections.AResponderIdTipoProjection;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface AResponderJpaRepository extends BaseContentRepositorio<AResponderEntity> {

    @Query(value = """
            -- mismo SQL recursivo que Repository/AResponderRepository.getIssueItems, sin cambios
            """, nativeQuery = true)
    List<QuestionnaireItem> getIssueItems(@Param("id") Long id);

    @Query(value = """
            -- mismo SQL recursivo que Repository/AResponderRepository.getCriticsIdsForQuestion, sin cambios
            """, nativeQuery = true)
    ArrayList<Long> getCriticsIdsForQuestion(@Param("id") Long id);

    @Query(value = """
            WITH RECURSIVE TODO_EL_CONTENIDO_DEL_TEMA AS (
                SELECT ID, id_del_duenio, tipo, 1 as nivel
                FROM aresponder WHERE id = :id
                UNION ALL
                SELECT ar.ID, ar.id_del_duenio, ar.tipo, nivel + 1
                FROM aresponder ar
                INNER JOIN TODO_EL_CONTENIDO_DEL_TEMA sp ON sp.id = ar.id_del_duenio
            )
            SELECT id, tipo as type
            FROM TODO_EL_CONTENIDO_DEL_TEMA
            WHERE nivel > 1
            """, nativeQuery = true)
    List<AResponderIdTipoProjection> findDescendantIds(@Param("id") Long id);
}
```

```java
// dentro de TemarioJpaAdapter, nuevo dispatch en @PostConstruct
private final Map<TipoAResponder, Consumer<Long>> mapDeBorrado = new HashMap<>();

@PostConstruct
private void init() {
    mapDeBorrado.put(TipoAResponder.TEMA, temarioJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.SUBTEMA, temarioJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.PREGUNTA_SIMPLE, preguntaSimpleJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.VERDADERO_FALSO, verdaderoOFalsoJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.SELECCION_UNICA, seleccionUnicaJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.OPCION_MULTIPLE, opcionMultipleJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, desplegableCompartidoJpaRepository::deleteById);
    mapDeBorrado.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, desplegableIndependienteJpaRepository::deleteById);
}

@Override
public void deleteById(Long id) {
    aResponderJpaRepository.findDescendantIds(id)
            .forEach(descendiente -> mapDeBorrado.get(descendiente.getType()).accept(descendiente.getId()));
    temarioJpaRepository.deleteById(id);
}
```

`mapDeBorrado` no registra `CUESTIONARIO` — un `CUESTIONARIO` nunca debería aparecer como descendiente de otro nodo (es siempre la raíz del árbol), consistente con la regla de negocio ya existente ("`SUBTEMA` no puede contener otro `Temario`"). Si apareciera, `mapDeBorrado.get(...)` devuelve `null` y el `.accept()` lanza `NullPointerException` — documentado en Riesgos.

---

## Plan de implementación

1. **Crear la proyección `AResponderIdTipoProjection`** en `projections/`.
2. **Crear `AResponderJpaRepository`** en `content/infrastructure/persistence/repository/`, con `getIssueItems`, `getCriticsIdsForQuestion` (SQL copiado tal cual) y `findDescendantIds` (SQL nuevo, ver Modelo de datos). El sistema sigue compilando y funcionando igual (nadie usa esta interfaz todavía).
3. **Modificar `TemarioJpaAdapter`:**
   a. Inyectar `AResponderJpaRepository` y los 6 `*JpaRepository` de pregunta; quitar `temarioRepositoryViejo` y `aResponderRepositoryViejo` (campos + parámetros de constructor).
   b. Construir `mapDeBorrado` en `@PostConstruct` (ver Modelo de datos).
   c. Reescribir `deleteById` para recorrer `findDescendantIds` + despachar por tipo + borrar el nodo raíz al final.
   d. `findDirectChildren`/`findIssueItems`/`findCriticalQuestionIds` pasan a usar `AResponderJpaRepository`.
4. **Modificar `AResponderTipoLookupJpaAdapter`:** inyectar `AResponderJpaRepository`; `findTipoById` pasa a `aResponderJpaRepository.findById(id).map(AResponderEntity::getTipo)`.
5. **Grep de verificación:** confirmar que ningún archivo en `src/main` ni `src/test` importa o referencia `com.lorenzomar3.AQ.Repository.AResponderRepository` ni `com.lorenzomar3.AQ.Repository.TemarioRepository` fuera de los propios archivos.
6. **Borrar `Repository/AResponderRepository.java` y `Repository/TemarioRepository.java`.**
7. **Limpiar imports muertos en `CreateQuestionResponseDTO.java`** (`AResponder`, `TipoCuestionario`).
8. **Escribir los tests de integración nuevos** (`@DataJpaTest` + `@AutoConfigureTestDatabase(replace = Replace.NONE)`, Postgres real vía `docker-compose`):
   a. `AResponderJpaRepository`: `getIssueItems`, `getCriticsIdsForQuestion`, `findDescendantIds` — árbol de ejemplo con cuestionario → tema → subtema → preguntas de al menos 2 tipos, alguna en estado crítico.
   b. `TemarioJpaAdapter.deleteById`: borrar un nodo con descendientes de varios tipos y verificar (vía los `*JpaRepository` correspondientes) que ninguna fila del subárbol sobrevive.
9. **Verificación.** Correr `./mvnw test` (lo corre el usuario) — debe compilar y pasar completo, incluyendo los tests nuevos. Correr `./mvnw package`. Verificación manual contra `AQ-SIMPLE-FRONT`: `GET /issues/{id}/items`, `GET /issues/{id}/question-ids`, `GET /questions/{id}/critical-ids`, y **`DELETE /issues/{id}` sobre un tema con hijos reales** (el caso que antes dependía del cascade de Hibernate).

---

## Criterios de aceptación

- [ ] `AResponderIdTipoProjection` existe en `projections/` con `getId()`/`getType()`.
- [ ] `AResponderJpaRepository` existe en `content/infrastructure/persistence/repository/`, extiende `BaseContentRepositorio<AResponderEntity>`, con `getIssueItems`/`getCriticsIdsForQuestion` (SQL idéntico al original) y `findDescendantIds` (SQL nuevo).
- [ ] `TemarioJpaAdapter` ya no tiene los campos `temarioRepositoryViejo` ni `aResponderRepositoryViejo`.
- [ ] `TemarioJpaAdapter.deleteById` borra el nodo pedido y **todos** sus descendientes (de cualquier tipo de pregunta y de tipo `TEMA`/`SUBTEMA`), sin depender de ningún cascade de Hibernate.
- [ ] `TemarioJpaAdapter.findDirectChildren`/`findIssueItems`/`findCriticalQuestionIds` usan `AResponderJpaRepository`.
- [ ] `AResponderTipoLookupJpaAdapter` ya no depende de `com.lorenzomar3.AQ.Repository.AResponderRepository`; usa `AResponderJpaRepository` y `AResponderEntity::getTipo`.
- [ ] `Repository/AResponderRepository.java` y `Repository/TemarioRepository.java` no existen en el repositorio.
- [ ] `CreateQuestionResponseDTO.java` ya no importa `AResponder` ni `TipoCuestionario`.
- [ ] Ningún archivo bajo `src/main` o `src/test` referencia `Repository.AResponderRepository` ni `Repository.TemarioRepository` (verificado por grep).
- [ ] Existen tests de integración nuevos cubriendo `getIssueItems`, `getCriticsIdsForQuestion`, `findDescendantIds` y el borrado en cascada de `TemarioJpaAdapter.deleteById`, corriendo contra Postgres real.
- [ ] `model/AResponder/**`, `PostPreguntaDTO`, `RespuestaDePreguntaDTO`, `PreguntaController`, `EliminarIssueService`, `TemarioRepositoryPort`, `AResponderTipoLookupPort` no fueron modificados (fuera de alcance de este spec).
- [ ] `./mvnw test` corre completo y pasa — **pendiente, lo corre el usuario.**
- [ ] `./mvnw package` compila sin errores — **pendiente, lo corre el usuario.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT` de `GET /issues/{id}/items`, `GET /issues/{id}/question-ids`, `GET /questions/{id}/critical-ids` y `DELETE /issues/{id}` sobre un tema con hijos reales — **pendiente, lo corre el usuario.**

---

## Decisiones tomadas

- **Se divide la migración de "AResponder/Repository legacy" en dos specs (Frente A / Frente B) en vez de uno solo.** El Frente A (este spec) es acotado y sin dependencia del B. El Frente B (reemplazo de los value types embebidos en `PostPreguntaDTO`/`RespuestaDePreguntaDTO` y reescritura de `PreguntaController.PUT`, para poder finalmente borrar `model/AResponder/**`) es una migración de contratos de entrada/salida bastante más grande y se deja para un spec futuro, como ya lo había anticipado el spec 16.
- **`deleteById` se reescribe con borrado en cascada explícito, en vez de dejarlo apoyado en el cascade de Hibernate legacy.** Al diseñar el spec se detectó que `TemarioJpaAdapter.deleteById` dependía de `@OneToMany(cascade = CascadeType.ALL)` en la entidad `Temario` legacy — sin esa entidad, borrar un tema con hijos dejaría filas huérfanas (no hay `ON DELETE CASCADE` a nivel de constraint de DB). Se evaluó como alternativa más simple no tocar `deleteById` y dejar `Repository/TemarioRepository.java` vivo solo por este método, pero se descartó: el usuario prefirió cerrar el Frente A completo (ambos repos legacy borrados) aunque implique más código nuevo.
- **El recorrido del subárbol a borrar usa una query nativa nueva (`findDescendantIds`) en vez de BFS iterativo sobre `findDirectChildren`.** Un solo round-trip a DB, y reutiliza el mismo patrón de CTE recursivo (`WITH RECURSIVE TODO_EL_CONTENIDO_DEL_TEMA`) ya probado en las otras dos queries portadas — menos código nuevo tipo-Java que un recorrido BFS manual, a cambio de una query SQL adicional que hay que verificar con test.
- **El despacho de borrado por tipo (`mapDeBorrado`) vive dentro de `TemarioJpaAdapter`, no en `EliminarIssueService`.** Es un reemplazo directo de lo que antes hacía el cascade de Hibernate — un detalle de cómo se persiste/borra el árbol, no una regla de negocio. Mantener el contrato de `TemarioRepositoryPort.deleteById` ("borra el nodo y todo su subárbol") sin cambios evita que `EliminarIssueService` (y cualquier otro consumidor futuro del puerto) necesite saber nada sobre los 6 tipos de pregunta.
- **El nuevo repositorio se llama `AResponderJpaRepository` y no se agregan sus métodos a `TemarioJpaRepository`.** Las queries operan a nivel `AResponder` (cualquier id del árbol, no solo temarios), y tanto `TemarioJpaAdapter` como `AResponderTipoLookupJpaAdapter` las necesitan.
- **El SQL de las dos queries portadas (`getIssueItems`, `getCriticsIdsForQuestion`) se copia sin modificar ni "optimizar"**, pese a los comentarios `//Analizar.` en el original que ya señalaban candidatas a revisión de performance. Se deja como deuda documentada, no como parte de este spec. La query nueva (`findDescendantIds`) sí es SQL genuinamente nuevo, necesario para el cascade delete.
- **El test de integración nuevo corre contra Postgres real (`docker-compose`), no H2.** Las queries usan sintaxis específica de Postgres (`WITH RECURSIVE`, `BOOL_OR`) que no está garantizado que H2 soporte de forma idéntica.
- **Se limpian los imports muertos de `CreateQuestionResponseDTO` en este spec** — cambio de una línea, cero riesgo, y de paso reduce el conteo de archivos que "importan `model.AResponder`" de cara al Frente B futuro.

---

## Riesgos identificados

- **`mapDeBorrado` no cubre `CUESTIONARIO`.** Si `findDescendantIds` alguna vez devolviera un descendiente de tipo `CUESTIONARIO` (no debería ocurrir según las reglas de negocio actuales — un `CUESTIONARIO` siempre es raíz), `mapDeBorrado.get(...)` devuelve `null` y `.accept()` lanza `NullPointerException`, tumbando el borrado completo a mitad de camino (algunos descendientes ya borrados, otros no). *Mitigación:* ninguna nueva en este spec — mismo trade-off ya aceptado en spec 15 para los `Map` de dispatch existentes (fallar explícito ante un tipo no registrado en vez de fallar silencioso). Si se agrega un séptimo tipo de pregunta en el futuro sin registrarlo en `mapDeBorrado`, el borrado de cualquier tema que lo contenga como descendiente rompe de la misma forma — deuda conocida y heredada del patrón existente.
- **`findDescendantIds` es SQL nuevo, escrito para este spec, sin historial de uso en producción** (a diferencia de `getIssueItems`/`getCriticsIdsForQuestion`, que son SQL ya probado en producción, solo reubicado). Un error de transcripción en el CTE recursivo podría dejar descendientes sin borrar (filas huérfanas, mismo problema que se busca resolver) o borrar de más. *Mitigación:* test de integración dedicado (paso 8.b) que verifica explícitamente que ninguna fila del subárbol sobrevive tras `deleteById`, más verificación manual contra `AQ-SIMPLE-FRONT` sobre un tema con hijos reales antes de dar el spec por cerrado.
- **El borrado deja de ser una única operación transactional simple a nivel ORM y pasa a ser N+1 `deleteById` individuales (uno por descendiente + el nodo raíz) dentro de `TemarioJpaAdapter.deleteById`.** Si el método no está `@Transactional` (o el `@Transactional` de `EliminarIssueService.eliminar` no cubre correctamente esta cadena de llamadas), un fallo a mitad del borrado podría dejar el árbol parcialmente borrado. *Mitigación:* `EliminarIssueService.eliminar` ya es `@Transactional` y no cambia — todas las llamadas a `deleteById` disparadas desde `TemarioJpaAdapter` ocurren dentro de esa misma transacción, por lo que un fallo revierte todo. Se verifica explícitamente en el test de integración del paso 8.b (fallo simulado no incluido, pero el happy path confirma que todas las filas desaparecen en una sola operación).
- **Superficie de este spec creció respecto al análisis inicial** (de "portar 2 queries" a "portar 2 queries + escribir borrado en cascada nuevo"), lo cual amplía el área de cambio en `TemarioJpaAdapter` más de lo que el objetivo original sugería. *Mitigación:* ninguna necesaria — fue una decisión explícita del usuario al elegir cerrar el Frente A completo en vez de dejar `TemarioRepository.java` vivo solo por `deleteById`.

---

## Próximos pasos — Frente B (pendiente, sin spec propio todavía)

Nota de arranque para la próxima sesión, no un plan comprometido. Cuando se redacte el spec 18 (con `/spec`), partir de acá:

- **Objetivo del Frente B:** eliminar la última dependencia de producción sobre `model/AResponder/**`, para poder borrar el árbol de entidades legacy completo. Hoy ese árbol sigue vivo únicamente porque `PostPreguntaDTO`, `RespuestaDePreguntaDTO` y `PreguntaController.PUT` (`updateQuestion`) todavía lo referencian.
- **Puntos concretos de entrada, confirmados por lectura de código en esta sesión:**
  - `dto/newDto/PostPreguntaDTO.java` — record que importa y usa directamente `TeoriaDeLaPregunta`, `Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente` (todos bajo `model.AResponder.**`) como tipos de sus campos.
  - `dto/newDto/RespuestaDePreguntaDTO.java` — mismo problema: `Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente` como tipos de campo.
  - `Controller/PreguntaController.java`, método `updateQuestion` (`PUT /questions`, línea ~233): recibe `PostPreguntaDTO`, despacha por tipo vía `mapDeEdicion`, y cada rama (`editarPreguntaSimple`, etc.) construye a mano un `model.AResponder.Pregunta` (o subtipo) legacy para la respuesta HTTP (`@JsonView(View.JustToAnswer.class)` sobre `Pregunta`).
- **Por qué es más grande que el Frente A:** no es portar una query, es reemplazar tipos que viajan en contratos de entrada/salida de la API (DTOs de request/response), lo que probablemente obliga a: (a) crear value types hexagonales equivalentes para `TeoriaDeLaPregunta`/`Opcion`/`OpcionDeDesplegableCompartido`/`SeleccionUnicaParaDesplegableIndependiente`, (b) reescribir `PostPreguntaDTO`/`RespuestaDePreguntaDTO` para usarlos, y (c) reescribir `updateQuestion` para devolver algo construido desde el dominio hexagonal en vez de `model.AResponder.Pregunta`, sin romper el shape JSON que ya consume `AQ-SIMPLE-FRONT`.
- **No investigado todavía:** si `RespuestaDePreguntaDTO` tiene otros consumidores además del flujo de verificación (`answering/`), y si `EditarXxxUseCase`/`EditarXxxService` (los 6 casos de uso de edición) ya devuelven objetos de dominio hexagonal puros o si también arrastran el mismo problema puertas adentro.
