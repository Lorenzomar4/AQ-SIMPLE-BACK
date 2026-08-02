# Spec 14 — Migración hexagonal: `POST /questions/verify` para los 4 tipos restantes

**Estado:** Draft
**Dependencias:** Spec 13 (`answering/` slice, `EstadoCritico`, patrón de `VerificarRespuestaController` y sus Use Cases/adapters) — se extiende el mismo slice y patrón. Specs 03-06 (`SeleccionUnicaRepositoryPort`/`JpaRepository`, `OpcionMultipleRepositoryPort`/`JpaRepository`, `DesplegableCompartidoRepositoryPort`/`JpaRepository`, `DesplegableIndependienteRepositoryPort`/`JpaRepository`, y sus `Entity` JPA) — se reutilizan directo desde la infraestructura de `answering`, mismo patrón cross-slice ya usado en spec 13 con specs 01-02.
**Fecha:** 2026-08-01
**Objetivo:** Migrar `POST /questions/verify` a arquitectura hexagonal para los 4 tipos de pregunta restantes (`SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`), completando el slice `answering/` para los 6 tipos de pregunta y eliminando el fallback a `PreguntaService.verifyResponse` (viejo) del dispatch de `VerificarRespuestaController`.

---

## Alcance

### Incluido

- **`answering/domain/` (nuevo, Java puro, sin anotaciones de framework):**
  - `Opcion` (nuevo, propio de `answering` — no confundir con `model.AResponder.TiposDePreguntas.Opcion` viejo ni con `content.domain.Opcion`) — campos `Long id`, `Boolean laRespuestaEs`. Objeto de dominio interno usado por `SeleccionUnicaParaResponder`/`OpcionMultipleParaResponder` para representar tanto la opción almacenada como la opción que manda el usuario.
  - `SeleccionUnicaParaResponder` — campos `Long id`, `List<Opcion> listaDeOpciones`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(List<Opcion> respuestaDelUsuario)`: primero valida cardinalidad (`BussinesException` si la lista del usuario no tiene **exactamente una** opción con `laRespuestaEs = true` — replica `validacionDeOpcionUnica`/`existeUnaOpcionVerdaderaUnicamente`), luego compara **cada opción de la respuesta del usuario** contra el mapa `id → laRespuestaEs` construido desde `listaDeOpciones` (`allMatch`), delega en `estadoCritico.actualizar(...)`, devuelve la corrección. Preserva tal cual: lista de usuario vacía pasa la validación de cardinalidad como inválida (0 ≠ 1, lanza excepción — a diferencia de `OpcionMultiple` donde vacía sí es válida y "correcta"); id inexistente en el mapa real → `NullPointerException` tal cual el viejo (no se atrapa).
  - `OpcionMultipleParaResponder` — campos `Long id`, `List<Opcion> listaDeOpciones`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(List<Opcion> respuestaDelUsuario)`: **sin** validación de cardinalidad (preserva que `OpcionMultiple` no valida nada), misma comparación `allMatch` contra el mapa `id → laRespuestaEs`, delega en `estadoCritico.actualizar(...)`. Preserva tal cual: lista de usuario vacía → `allMatch` sobre vacío es `true` → "respuesta correcta"; id inexistente → `NullPointerException` tal cual.
  - `OpcionDeDesplegableCompartido` (nuevo, propio de `answering`) — campos `Long id`, `String respuesta`.
  - `DesplegableCompartidoParaResponder` — campos `Long id`, `List<OpcionDeDesplegableCompartido> listaDeOpciones`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(List<OpcionDeDesplegableCompartido> respuestaDelUsuario)`: compara cada opción del usuario contra el mapa `id → respuesta` (String) construido desde `listaDeOpciones` (`allMatch`), delega en `estadoCritico.actualizar(...)`. Preserva tal cual: lista vacía → `true`; id inexistente → `NullPointerException` tal cual.
  - `OpcionDeSeleccionParaDesplegableIndependiente` (nuevo, propio de `answering`) — campos `Long id`, `Boolean respuestaCorrecta`.
  - `SeleccionUnicaParaDesplegableIndependiente` (nuevo, propio de `answering`) — campos `Long id`, `List<OpcionDeSeleccionParaDesplegableIndependiente> listaDeOpcionesDisponible`. Método `Long getRespuestaCorrecta()`: `listaDeOpcionesDisponible.stream().filter(OpcionDeSeleccionParaDesplegableIndependiente::getRespuestaCorrecta).toList().get(0).getId()` — replica tal cual el `.get(0)` sin chequeo (`IndexOutOfBoundsException` si ninguna opción está marcada correcta).
  - `DesplegableIndependienteParaResponder` — campos `Long id`, `List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpciones`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(List<SeleccionUnicaParaDesplegableIndependiente> respuestaDelUsuario)`: compara cada desplegable de la respuesta del usuario (`getRespuestaCorrecta()`, el id que el usuario marcó) contra el mapa `id del desplegable → getRespuestaCorrecta() real` construido desde `listaDeOpciones` (`allMatch`), delega en `estadoCritico.actualizar(...)`. Preserva tal cual: lista vacía → `true`; id de desplegable inexistente → `NullPointerException` tal cual; desplegable real sin opción correcta marcada → `IndexOutOfBoundsException` al construir el mapa (se propaga tal cual, sin atrapar).
- **`answering/application/port/out/` (nuevo):** `SeleccionUnicaParaResponderPort`, `OpcionMultipleParaResponderPort`, `DesplegableCompartidoParaResponderPort`, `DesplegableIndependienteParaResponderPort` — cada uno con `findById(Long): Optional<Xxx>` y `save(Xxx): Xxx`.
- **`answering/application/port/in/` y `application/service/` (nuevo):** `VerificarRespuestaSeleccionUnicaUseCase`/`Service`, `VerificarRespuestaOpcionMultipleUseCase`/`Service`, `VerificarRespuestaDesplegableCompartidoUseCase`/`Service`, `VerificarRespuestaDesplegableIndependienteUseCase`/`Service` — cada uno resuelve vía su port (`BussinesException` si no existe), mapea la porción correspondiente del DTO (`respuesta.listaDeOpciones()`, `respuesta.listaDeOpcionesParaDesplegableCompartidos()`, `respuesta.listaDeSeleccionesUnicasParaDesplegableIndependiente()` — clases viejas del DTO) hacia los objetos de dominio nuevos de `answering` (mapeo inline en el service, sin mapper separado), llama `verificarRespuesta(...)`, guarda el resultado vía `save`, devuelve el booleano.
- **`answering/infrastructure/persistence/adapter/` (nuevo):** `SeleccionUnicaParaResponderAdapter`, `OpcionMultipleParaResponderAdapter`, `DesplegableCompartidoParaResponderAdapter`, `DesplegableIndependienteParaResponderAdapter` — implementan los ports inyectando **directamente** `content.infrastructure.persistence.repository.SeleccionUnicaJpaRepository`/`OpcionMultipleJpaRepository`/`DesplegableCompartidoJpaRepository`/`DesplegableIndependienteJpaRepository` (specs 03-06). Convierten `Entity` (de `content`) ↔ objeto de dominio de `answering`, conversión inline en el propio adapter. `save` solo persiste el campo del contador de crítico (misma estrategia que spec 13: lee la entity de nuevo, actualiza `intentosParaQueDejeDeSerCriticoDisponible`, guarda) — las listas de opciones no se reescriben en este flujo (son de solo lectura para `verify`).
- **`answering/infrastructure/controller/VerificarRespuestaController` (modificado):** dispatch explícito por los 6 tipos vía `Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>>` (mismo patrón `@PostConstruct` que `EliminarPreguntaPorIdService`/`ObtenerIdsAleatoriosDePreguntasService`, specs 09/12) — cualquier tipo no registrado en el `Map` (ninguno debería llegar, dado que `TipoAResponder` solo tiene 6 tipos hoja + 3 contenedores que nunca deberían pegarle a `/verify`) lanza `BussinesException`. Se elimina la inyección de `PreguntaService` y su rama `else` de fallback.
- **Tests:** paridad de corrección para los 4 tipos contra `PreguntaService.verifyResponse` (viejo), incluida la preservación explícita de cada comportamiento raro documentado arriba (cardinalidad en Selección Única, lista vacía = correcta en Opción Múltiple/Desplegable Compartido/Desplegable Independiente, `NullPointerException` por id inexistente, `IndexOutOfBoundsException` en Desplegable Independiente sin opción correcta marcada); actualización del contador de crítico en ambos sentidos para los 4 tipos; not-found (`BussinesException`) para los 4 tipos; el dispatch nuevo del controller cubre los 6 tipos sin fallback.

### Explícitamente NO incluido

- Corregir cualquiera de los comportamientos raros documentados (cardinalidad, listas vacías, `NullPointerException`, `IndexOutOfBoundsException`) — se preservan tal cual, decisión ya tomada.
- Cambiar `RespuestaDePreguntaDTO` o las clases viejas que referencia (`model.AResponder.TiposDePreguntas.Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`) — se reutilizan tal cual como origen del mapeo, mismo criterio de "DTO raíz, no se toca" de specs 07-13.
- Relocar `fetch`/`fetch-full` (spec 11), `random-ids`/`critical-ids` (spec 12) a `answering/` — sigue siendo deuda diferida, documentada en spec 13.
- `PreguntaService.verifyResponse` no se elimina del código — queda sin caller real desde `VerificarRespuestaController`, pero se mantiene como baseline de los tests de paridad (mismo criterio de no tocar código fuera del camino migrado).
- `POST /questions/inverse` — queda para un spec futuro.
- Implementar SM-2 / repetición espaciada.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `content.infrastructure.persistence.entity.SeleccionUnicaEntity` / `SeleccionUnicaJpaRepository` (spec 03) — inyectado directo desde `answering.infrastructure.persistence.adapter.SeleccionUnicaParaResponderAdapter`.
- `content.infrastructure.persistence.entity.OpcionMultipleEntity` / `OpcionMultipleJpaRepository` (spec 04) — mismo criterio.
- `content.infrastructure.persistence.entity.DesplegableCompartidoEntity` / `DesplegableCompartidoJpaRepository` (spec 05) — mismo criterio.
- `content.infrastructure.persistence.entity.DesplegableIndependienteEntity` / `DesplegableIndependienteJpaRepository` (spec 06) — mismo criterio.
- `content.infrastructure.persistence.entity.OpcionEntity` (`id`, `opcion` String, `laRespuestaEs` Boolean) — anidada dentro de `SeleccionUnicaEntity`/`OpcionMultipleEntity`, y dentro de `SeleccionUnicaParaDesplegableIndependienteEntity.listaDeOpciones` (specs 03/04/06).
- `content.infrastructure.persistence.entity.OpcionDeDesplegableCompartidoEntity` (`id`, `pregunta` String, `respuesta` String) — anidada dentro de `DesplegableCompartidoEntity` (spec 05).
- `content.infrastructure.persistence.entity.SeleccionUnicaParaDesplegableIndependienteEntity` (`id`, `titulo`, `List<OpcionEntity> listaDeOpciones`) — anidada dentro de `DesplegableIndependienteEntity` (spec 06).
- `com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO(Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana, List<Opcion> listaDeOpciones, List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos, List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente)` — DTO de request, reutilizado tal cual, incluidas sus referencias a las clases viejas `model.AResponder.TiposDePreguntas.Opcion`, `model.AResponder.DesplegableCompartido.OpcionDeDesplegableCompartido`, `model.AResponder.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente` — mismo criterio que spec 13 con `RespuestaDePreguntaDTO` completo.
- `com.lorenzomar3.AQ.exception.BussinesException`, `com.lorenzomar3.AQ.model.TipoAResponder` — mismo uso estándar.
- `Service.PreguntaService.verifyResponse` (viejo) — se reutiliza tal cual como fallback de tests de paridad (ya no como fallback en el controller, ver más abajo).
- `answering.domain.EstadoCritico` (spec 13) — reutilizado tal cual, sin modificar, para los 4 objetos de dominio nuevos.

### `answering/domain/` (nuevo, Java puro — sin `@Entity`, `@JsonView` ni imports de Spring/JPA/Jackson, ni imports de `model.AResponder.*` ni `content.domain.*`)

```java
public class Opcion {
    private Long id;
    private Boolean laRespuestaEs;
}

public class SeleccionUnicaParaResponder {
    private Long id;
    private List<Opcion> listaDeOpciones;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(List<Opcion> respuestaDelUsuario) {
        long marcadasVerdaderas = respuestaDelUsuario.stream().filter(Opcion::getLaRespuestaEs).count();
        if (marcadasVerdaderas != 1) {
            throw new BussinesException("¡Asegurese de que haya solamente una opcion valida!");
        }
        Map<Long, Boolean> real = listaDeOpciones.stream()
            .collect(Collectors.toMap(Opcion::getId, Opcion::getLaRespuestaEs));
        boolean esCorrecta = respuestaDelUsuario.stream()
            .allMatch(o -> real.get(o.getId()).equals(o.getLaRespuestaEs()));
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}

public class OpcionMultipleParaResponder {
    private Long id;
    private List<Opcion> listaDeOpciones;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(List<Opcion> respuestaDelUsuario) {
        Map<Long, Boolean> real = listaDeOpciones.stream()
            .collect(Collectors.toMap(Opcion::getId, Opcion::getLaRespuestaEs));
        boolean esCorrecta = respuestaDelUsuario.stream()
            .allMatch(o -> real.get(o.getId()).equals(o.getLaRespuestaEs()));
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}

public class OpcionDeDesplegableCompartido {
    private Long id;
    private String respuesta;
}

public class DesplegableCompartidoParaResponder {
    private Long id;
    private List<OpcionDeDesplegableCompartido> listaDeOpciones;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(List<OpcionDeDesplegableCompartido> respuestaDelUsuario) {
        Map<Long, String> real = listaDeOpciones.stream()
            .collect(Collectors.toMap(OpcionDeDesplegableCompartido::getId, OpcionDeDesplegableCompartido::getRespuesta));
        boolean esCorrecta = respuestaDelUsuario.stream()
            .allMatch(o -> real.get(o.getId()).equals(o.getRespuesta()));
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}

public class OpcionDeSeleccionParaDesplegableIndependiente {
    private Long id;
    private Boolean respuestaCorrecta;
}

public class SeleccionUnicaParaDesplegableIndependiente {
    private Long id;
    private List<OpcionDeSeleccionParaDesplegableIndependiente> listaDeOpcionesDisponible;

    public Long getRespuestaCorrecta() {
        return listaDeOpcionesDisponible.stream()
            .filter(OpcionDeSeleccionParaDesplegableIndependiente::getRespuestaCorrecta)
            .toList().get(0).getId(); // preserva el .get(0) sin chequeo del modelo viejo
    }
}

public class DesplegableIndependienteParaResponder {
    private Long id;
    private List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpciones;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(List<SeleccionUnicaParaDesplegableIndependiente> respuestaDelUsuario) {
        Map<Long, Long> real = listaDeOpciones.stream()
            .collect(Collectors.toMap(SeleccionUnicaParaDesplegableIndependiente::getId,
                                       SeleccionUnicaParaDesplegableIndependiente::getRespuestaCorrecta));
        boolean esCorrecta = respuestaDelUsuario.stream()
            .allMatch(o -> real.get(o.getId()).equals(o.getRespuestaCorrecta()));
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
```

Todas con getters/setters vía Lombok `@Getter`/`@Setter` (mismo criterio que specs anteriores de `answering`/`content`), sin comportamiento adicional fuera del mostrado. Nota: `real.get(o.getId())` puede devolver `null` si el id no existe en la lista real — `.equals(...)` sobre eso lanza `NullPointerException`, preservando tal cual el comportamiento del `Verificador` viejo.

### `answering/application/port/out/` (nuevo)

- **`SeleccionUnicaParaResponderPort`** — `Optional<SeleccionUnicaParaResponder> findById(Long id)`; `SeleccionUnicaParaResponder save(SeleccionUnicaParaResponder pregunta)`.
- **`OpcionMultipleParaResponderPort`** — mismos métodos con `OpcionMultipleParaResponder`.
- **`DesplegableCompartidoParaResponderPort`** — mismos métodos con `DesplegableCompartidoParaResponder`.
- **`DesplegableIndependienteParaResponderPort`** — mismos métodos con `DesplegableIndependienteParaResponder`.

### `answering/application/port/in/` y `application/service/` (nuevo)

- **`VerificarRespuestaSeleccionUnicaUseCase`** — `Boolean verificar(RespuestaDePreguntaDTO respuesta)`. **`Service`** — inyecta `SeleccionUnicaParaResponderPort`; `findById` (`BussinesException` si no existe); mapea `respuesta.listaDeOpciones()` (viejo `Opcion`) → `List<answering.domain.Opcion>` inline; llama `verificarRespuesta(...)`; guarda; devuelve el booleano.
- **`VerificarRespuestaOpcionMultipleUseCase`/`Service`** — mismo patrón con `OpcionMultipleParaResponderPort`.
- **`VerificarRespuestaDesplegableCompartidoUseCase`/`Service`** — mismo patrón con `DesplegableCompartidoParaResponderPort`; mapea `respuesta.listaDeOpcionesParaDesplegableCompartidos()` (viejo `OpcionDeDesplegableCompartido`) → `List<answering.domain.OpcionDeDesplegableCompartido>`.
- **`VerificarRespuestaDesplegableIndependienteUseCase`/`Service`** — mismo patrón con `DesplegableIndependienteParaResponderPort`; mapea `respuesta.listaDeSeleccionesUnicasParaDesplegableIndependiente()` (viejo `SeleccionUnicaParaDesplegableIndependiente`, con `Set<Opcion> listaDeOpcionesDisponible`) → `List<answering.domain.SeleccionUnicaParaDesplegableIndependiente>` (cada `Opcion` del `Set` viejo se mapea a `OpcionDeSeleccionParaDesplegableIndependiente`).

### `answering/infrastructure/persistence/adapter/` (nuevo)

- **`SeleccionUnicaParaResponderAdapter`** — inyecta `SeleccionUnicaJpaRepository` (de `content`); `findById` mapea `SeleccionUnicaEntity` → `SeleccionUnicaParaResponder` (`id`, `listaDeOpciones` desde `OpcionEntity`, `estadoCritico` desde `entity.getIntentosParaQueDejeDeSerCriticoDisponible()`); `save` relee la entity, actualiza solo `intentosParaQueDejeDeSerCriticoDisponible`, guarda.
- **`OpcionMultipleParaResponderAdapter`** — análogo con `OpcionMultipleJpaRepository`.
- **`DesplegableCompartidoParaResponderAdapter`** — análogo con `DesplegableCompartidoJpaRepository`, mapea `OpcionDeDesplegableCompartidoEntity` → `answering.domain.OpcionDeDesplegableCompartido`.
- **`DesplegableIndependienteParaResponderAdapter`** — análogo con `DesplegableIndependienteJpaRepository`, mapea `SeleccionUnicaParaDesplegableIndependienteEntity` (con `List<OpcionEntity> listaDeOpciones`) → `answering.domain.SeleccionUnicaParaDesplegableIndependiente` (con `List<OpcionDeSeleccionParaDesplegableIndependiente>`, cada `OpcionEntity.laRespuestaEs` → `respuestaCorrecta`).

### `answering/infrastructure/controller/VerificarRespuestaController` (modificado)

- Se reemplaza el `if/else` de 3 ramas (2 tipos + fallback) por dispatch explícito sobre los 6 `UseCase` (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO` de spec 13 + los 4 nuevos), vía `Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>>` construido en `@PostConstruct` (mismo patrón que `EliminarPreguntaPorIdService`/`ObtenerIdsAleatoriosDePreguntasService`, specs 09/12). Tipo no encontrado en el `Map` → `BussinesException`.
- Se elimina el campo `PreguntaService preguntaService` y su uso — ya no hay fallback.

### `Service/PreguntaService` (sin cambios)

`verifyResponse` no se modifica ni se elimina — queda sin caller productivo, usado solo como baseline en tests de paridad.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutilizan `pregunta`, `seleccion_unica`, `multiple_opcion`, `desplegable_compartido`, `pregunta_desplegable_ind` (y sus tablas de opciones anidadas) tal cual.

---

## Plan de implementación

1. **Dominio de `answering` — objetos base.** Crear `Opcion`, `OpcionDeDesplegableCompartido`, `OpcionDeSeleccionParaDesplegableIndependiente` en `answering/domain/`, campos según el modelo de datos. Java puro, sin dependencias de Spring/JPA/Jackson ni de `model.AResponder.*`/`content.domain.*`.
2. **Dominio de `answering` — objetos "para responder".** Crear `SeleccionUnicaParaResponder`, `OpcionMultipleParaResponder`, `DesplegableCompartidoParaResponder`, `DesplegableIndependienteParaResponder` (este último junto con `SeleccionUnicaParaDesplegableIndependiente`), cada uno con su `verificarRespuesta(...)` según el modelo de datos.
3. **Tests de dominio.** Tests unitarios (sin Spring) por cada uno de los 4 objetos `...ParaResponder`: caso correcto, caso incorrecto, actualización de `EstadoCritico` en ambos sentidos, y cada comportamiento raro (cardinalidad en `SeleccionUnicaParaResponder`; lista vacía = correcta en `OpcionMultipleParaResponder`/`DesplegableCompartidoParaResponder`/`DesplegableIndependienteParaResponder`; `NullPointerException` por id inexistente en los 4; `IndexOutOfBoundsException` en `SeleccionUnicaParaDesplegableIndependiente.getRespuestaCorrecta()` sin opción marcada).
4. **Ports de salida.** Crear `SeleccionUnicaParaResponderPort`, `OpcionMultipleParaResponderPort`, `DesplegableCompartidoParaResponderPort`, `DesplegableIndependienteParaResponderPort` en `answering/application/port/out/`.
5. **Adapters.** Crear los 4 adapters en `answering/infrastructure/persistence/adapter/`, inyectando los `JpaRepository` de `content` (specs 03-06) y mapeando manualmente contra las `Entity` correspondientes, según el modelo de datos.
6. **Use case y service — `SeleccionUnica`.** Crear `VerificarRespuestaSeleccionUnicaUseCase`/`Service`, inyectando `SeleccionUnicaParaResponderPort`, mapeando `respuesta.listaDeOpciones()` (viejo) → dominio nuevo, lanzando `BussinesException` si no existe.
7. **Use case y service — `OpcionMultiple`.** Mismo patrón con `OpcionMultipleParaResponderPort`.
8. **Use case y service — `DesplegableCompartido`.** Mismo patrón con `DesplegableCompartidoParaResponderPort`, mapeando `respuesta.listaDeOpcionesParaDesplegableCompartidos()`.
9. **Use case y service — `DesplegableIndependiente`.** Mismo patrón con `DesplegableIndependienteParaResponderPort`, mapeando `respuesta.listaDeSeleccionesUnicasParaDesplegableIndependiente()` (incluida la conversión del `Set<Opcion>` viejo a `List<OpcionDeSeleccionParaDesplegableIndependiente>`).
10. **Test de paridad — `SeleccionUnica`.** Comparar `VerificarRespuestaSeleccionUnicaService.verificar` contra `PreguntaService.verifyResponse` (viejo): caso correcto, incorrecto, y violación de cardinalidad (0 o ≥2 opciones marcadas verdaderas por el usuario) — mismo `BussinesException` en ambos caminos.
11. **Test de paridad — `OpcionMultiple`.** Mismo criterio, incluida la respuesta vacía del usuario como caso "correcta" en ambos caminos.
12. **Test de paridad — `DesplegableCompartido`.** Mismo criterio, incluida la respuesta vacía como "correcta" en ambos caminos.
13. **Test de paridad — `DesplegableIndependiente`.** Mismo criterio, incluida la respuesta vacía como "correcta" y el caso de un desplegable real sin ninguna opción marcada como correcta (`IndexOutOfBoundsException` en ambos caminos).
14. **Tests de not-found.** Id inexistente en los 4 Use Cases nuevos → `BussinesException`.
15. **Cablear `VerificarRespuestaController`.** Reemplazar el dispatch de 3 ramas por el `Map<TipoAResponder, Function<...>>` con los 6 tipos (2 de spec 13 + 4 nuevos); eliminar el campo `PreguntaService` y su import; tipo no soportado → `BussinesException`.
16. **Test de dispatch del controller.** Confirmar que los 6 tipos válidos resuelven al Use Case correcto y que un tipo no soportado (p. ej. un contenedor `CUESTIONARIO`/`TEMA`/`SUBTEMA`, si llega por error) lanza `BussinesException` en vez de caer a un fallback silencioso.
17. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: responder una pregunta de cada uno de los 4 tipos nuevos (correcta e incorrecta) confirmando que la corrección y el contador de "crítico" se comportan igual que antes; confirmar que el resto de endpoints (`fetch(-full)`, `random-ids`, `critical-ids`, CRUD de `questions`/`issues`, `verify` de `PREGUNTA_SIMPLE`/`VERDADERO_FALSO`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [ ] Existen `Opcion`, `OpcionDeDesplegableCompartido`, `OpcionDeSeleccionParaDesplegableIndependiente`, `SeleccionUnicaParaDesplegableIndependiente`, `SeleccionUnicaParaResponder`, `OpcionMultipleParaResponder`, `DesplegableCompartidoParaResponder`, `DesplegableIndependienteParaResponder` en `answering/domain/`, Java puro, sin anotaciones de framework ni imports de `model.AResponder.*`/`content.domain.*`.
- [ ] `SeleccionUnicaParaResponder.verificarRespuesta` lanza `BussinesException` si la respuesta del usuario no tiene exactamente una opción con `laRespuestaEs = true`, replicando `validacionDeOpcionUnica` del modelo viejo.
- [ ] `OpcionMultipleParaResponder.verificarRespuesta` no valida cardinalidad; una respuesta vacía del usuario se evalúa como correcta (`allMatch` sobre lista vacía), igual que el modelo viejo.
- [ ] `DesplegableCompartidoParaResponder.verificarRespuesta` compara por `id → respuesta` (String); una respuesta vacía del usuario se evalúa como correcta, igual que el modelo viejo.
- [ ] `DesplegableIndependienteParaResponder.verificarRespuesta` compara por `id del desplegable → id de la opción correcta`; una respuesta vacía del usuario se evalúa como correcta; un desplegable real sin ninguna opción marcada como correcta lanza `IndexOutOfBoundsException` al resolver `getRespuestaCorrecta()`, igual que el modelo viejo.
- [ ] Los 4 objetos `...ParaResponder` lanzan `NullPointerException` (sin atrapar) cuando la respuesta del usuario referencia un `id` que no existe en la lista real, igual que el `Verificador` viejo.
- [ ] `EstadoCritico.actualizar` (spec 13, sin modificar) se invoca desde los 4 objetos de dominio nuevos con el resultado de la verificación, mismo comportamiento ya validado en spec 13.
- [ ] Existen `SeleccionUnicaParaResponderPort`, `OpcionMultipleParaResponderPort`, `DesplegableCompartidoParaResponderPort`, `DesplegableIndependienteParaResponderPort` en `answering/application/port/out/`, implementados por adapters que reutilizan los `JpaRepository` de `content` (specs 03-06) sin pasar por los ports/domain/services de `content`.
- [ ] Existen `VerificarRespuestaSeleccionUnicaUseCase`/`Service`, `VerificarRespuestaOpcionMultipleUseCase`/`Service`, `VerificarRespuestaDesplegableCompartidoUseCase`/`Service`, `VerificarRespuestaDesplegableIndependienteUseCase`/`Service`, que lanzan `BussinesException` si el id no existe.
- [ ] `POST /questions/verify` para cada uno de los 4 tipos nuevos devuelve el mismo booleano que el camino viejo (`PreguntaService.verifyResponse`) y persiste el mismo valor de `intentosParaQueDejeDeSerCriticoDisponible`, para respuesta correcta e incorrecta.
- [ ] `POST /questions/verify` con un id inexistente de cualquiera de los 4 tipos nuevos lanza `BussinesException`.
- [ ] `VerificarRespuestaController` despacha los 6 tipos de pregunta (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO` de spec 13 + los 4 nuevos) a su Use Case correspondiente; ya no inyecta `PreguntaService` ni tiene rama de fallback.
- [ ] `POST /questions/verify` con un tipo no soportado por el `Map` de dispatch lanza `BussinesException` en vez de delegar silenciosamente a código viejo.
- [ ] El resto de endpoints (`fetch(-full)`, `random-ids`, `critical-ids`, CRUD de `questions`/`issues`, `verify` de `PREGUNTA_SIMPLE`/`VERDADERO_FALSO`) sigue funcionando sin cambios.
- [ ] Ningún archivo bajo `answering/` importa una clase de `content.application`, `content.domain` o `model.AResponder.*` — solo se reutilizan `content.infrastructure.persistence.entity.*` y `*.repository.*JpaRepository` (specs 03-06) desde los adapters de `answering`, y las clases viejas del DTO (`model.AResponder.TiposDePreguntas.Opcion`, etc.) se leen únicamente dentro de los services nuevos como origen del mapeo, sin propagarse al dominio.
- [ ] Existen tests cubriendo: lógica pura de los 4 objetos de dominio nuevos (incluidos todos los comportamientos raros), paridad de `verify` para los 4 tipos, not-found para los 4, y el dispatch completo del controller (6 tipos + tipo no soportado).
- [ ] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT`: responder una pregunta de cada uno de los 4 tipos nuevos (correcta e incorrecta) desde el flujo real, confirmando que la corrección y el contador de "crítico" se comportan igual que antes. **(verificado por el usuario)**
- [ ] `PreguntaService.verifyResponse` (viejo) no fue eliminado; queda sin caller productivo, usado solo como baseline de los tests de paridad.

---

## Decisiones tomadas y descartadas

- **Se migran los 4 tipos restantes en un solo spec, en vez de dividir en dos (Selección Única + Opción Múltiple por un lado, Desplegable Compartido + Desplegable Independiente por otro).**
  Confirmado con el usuario. Mismo criterio que spec 13 (migró 2 tipos juntos): los 4 comparten el mismo patrón de `EstadoCritico`/dispatch por `Map`, y cerrar `verify` por completo en un spec evita dejar el fallback viejo funcionando a medias para un subconjunto de tipos otra vez.

- **Los comportamientos raros identificados (validación de cardinalidad en Selección Única, lista vacía = respuesta correcta en Opción Múltiple/Desplegable Compartido/Desplegable Independiente, `NullPointerException` por id inexistente, `IndexOutOfBoundsException` en Desplegable Independiente sin opción correcta marcada) se preservan tal cual.**
  Confirmado con el usuario. Mismo criterio de paridad exacta ya usado en spec 13 con el comportamiento incompleto de `PreguntaSimple` — no se corrigen comportamientos de negocio fuera del alcance estricto de esta migración, salvo un leak de dato sensible (que no es el caso acá).

- **El mapeo entre las clases viejas del DTO (`model.AResponder.TiposDePreguntas.Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`) y el dominio nuevo de `answering` ocurre inline dentro de cada `Service` nuevo, sin tocar el DTO ni crear un mapper separado.**
  Confirmado con el usuario. Mismo criterio de "DTO raíz del proyecto, reutilizado tal cual" de specs 07-13; evita que la migración se convierta en un cambio de contrato de wire.

- **Se reescribe inline la lógica del `Verificador<T,G>` viejo en cada uno de los 4 objetos de dominio nuevo, en vez de reutilizar la clase `Verificador` original (Java puro, sin anotaciones de framework).**
  Confirmado con el usuario. Descartado: importar `model.AResponder.Verificador.Verificador` directo en `answering/domain` — aunque es código sin dependencias de framework y hubiera evitado repetir el algoritmo 3 veces, acopla `answering` a un paquete del modelo viejo destinado a desaparecer, rompiendo el mismo principio de aislamiento que llevó a reescribir `EstadoCritico` en spec 13 en vez de reutilizar `conteoDeCritico` de `Pregunta`.

- **No se extrae un helper genérico compartido (equivalente a `Verificador<T,G>`) dentro de `answering/domain` para las 3 comparaciones "id → valor esperado" (`Boolean`, `String`, `Long`), aunque tengan la misma forma algorítmica.**
  Justificación: a diferencia de `EstadoCritico` (spec 13, lógica idéntica bit a bit sin variación esperada), acá cada tipo compara un valor de naturaleza distinta y ya vive dentro de un método de dominio con nombre y contexto propios (`verificarRespuesta`); introducir un genérico shared hubiera sido la misma abstracción prematura que specs 10/12 decidieron evitar para estructuras parecidas pero no idénticas (`AResponderChildRef`/`AResponderItemDetail`, `TIPOS_CONTENEDOR`).

- **`Opcion` se modela como una clase nueva y propia de `answering/domain`, distinta de `model.AResponder.TiposDePreguntas.Opcion` (viejo) y de `content.domain.Opcion` (specs 03/04).**
  Es la tercera clase `Opcion` en el codebase. Mismo criterio ya aceptado en toda la migración (cada slice modela sus propios tipos, ver `PreguntaSimpleParaResponder`/`VerdaderoOFalsoParaResponder` en spec 13 frente a sus equivalentes en `content.domain`); se documenta explícitamente acá por el riesgo de confusión al ser el mismo nombre repetido 3 veces.

- **Se crean dos objetos de dominio separados, `SeleccionUnicaParaResponder` y `OpcionMultipleParaResponder`, en vez de una sola clase parametrizada por un flag de "valida cardinalidad".**
  Mismo criterio que el modelo viejo (dos subclases distintas de `Pregunta`) y que specs 03/04 (dos `RepositoryPort`/`Entity` separados) — evita una abstracción condicional (`if validarCardinalidad`) sobre dos tipos que ya son conceptualmente distintos para el dominio.

- **Reuso de infraestructura cross-slice: los 4 adapters nuevos de `answering` inyectan directo los `JpaRepository` de `content` (specs 03-06), sin duplicar `Entity`/`JpaRepository` propios.**
  Confirmado con el usuario. Mismo patrón y misma justificación que spec 13: el riesgo que `ARQUITECTURA.md` busca evitar (acoplar el dominio de `answering` a cambios internos de `content`) vive en `application`/`domain`, no en qué clase JPA lee una columna — esa capa queda 100% aislada.

- **`VerificarRespuestaController` elimina el fallback a `PreguntaService.verifyResponse` y lanza `BussinesException` para cualquier tipo no registrado, en vez de mantener el fallback "por si acaso".**
  Confirmado con el usuario. Con los 6 tipos hoja migrados, el fallback ya no tiene ningún caller válido posible (los 3 tipos contenedor — `CUESTIONARIO`/`TEMA`/`SUBTEMA` — nunca deberían llegar a `/verify`); mantenerlo hubiera sido código muerto disfrazado de red de seguridad, ocultando un bug de dispatch en vez de exponerlo con una excepción clara.

- **El dispatch del controller pasa de `if/else` (3 ramas en spec 13) a `Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>>` construido en `@PostConstruct`, replicando el patrón de `EliminarPreguntaPorIdService`/`ObtenerIdsAleatoriosDePreguntasService` (specs 09/12).**
  Justificación: con 6 ramas homogéneas (todas del mismo tipo `RespuestaDePreguntaDTO -> Boolean` vía su Use Case), el `Map` es más legible que un `if/else` de 6 ramas y sigue el mismo criterio ya usado en el resto de la arquitectura para dispatch por tipo con ramas triviales.

- **`PreguntaService.verifyResponse` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores — sigue sirviendo como baseline de los tests de paridad, aunque ya sin caller productivo tras este spec.

---

## Riesgos identificados

- **`content.infrastructure.persistence.repository.SeleccionUnicaJpaRepository`/`OpcionMultipleJpaRepository`/`DesplegableCompartidoJpaRepository`/`DesplegableIndependienteJpaRepository` quedan usados por dos adapters de dos slices distintos** (el propio de `content` y el nuevo de `answering`), igual que ya ocurrió con `PreguntaSimpleJpaRepository`/`VerdaderoOFalsoJpaRepository` en spec 13. Si `content` cambia la firma de alguno de estos 4 repositorios, rompe silenciosamente a `answering` también.
  *Mitigación:* ninguna adicional — mismo costo aceptado explícitamente por el usuario en spec 13, ahora extendido a 4 repositorios más; los tests de paridad de este spec y los de specs 03-06 actúan como red de contención.

- **La conversión `Entity` ↔ dominio de `answering` es manual e inline en cada uno de los 4 adapters nuevos, sin mapper compartido.** Mismo riesgo que spec 13, multiplicado por 4: si `SeleccionUnicaEntity`/`OpcionMultipleEntity`/`DesplegableCompartidoEntity`/`DesplegableIndependienteEntity` ganan un campo relevante, hay que recordar actualizar el mapeo de `answering` por separado.
  *Mitigación:* ninguna en este spec — mismo riesgo de "registro en múltiples puntos" ya aceptado en el resto de la arquitectura.

- **Existen ahora tres clases distintas llamadas `Opcion`** (`model.AResponder.TiposDePreguntas.Opcion`, `content.domain.Opcion`, `answering.domain.Opcion`), con formas parecidas pero sin relación de herencia ni conversión automática entre sí. Un futuro desarrollador que busque "Opcion" en el IDE puede editar la clase equivocada sin darse cuenta de en qué slice está.
  *Mitigación:* ninguna adicional — se documenta acá y queda mitigado en parte por el paquete distinto de cada una; mismo criterio de "cada slice modela lo suyo" ya aceptado desde specs 07+.

- **Los comportamientos raros preservados (`NullPointerException` por id inexistente, `IndexOutOfBoundsException` en Desplegable Independiente sin opción correcta, "lista vacía = correcta") quedan replicados en `answering/domain`, ahora sin ningún comentario ni contexto visible que explique que son intencionales** (a diferencia del código viejo, que al menos convivía con el resto de la lógica de `IPreguntaVariasOpciones`/`Verificador` donde un lector podía rastrear el origen). Alguien que audite `answering/domain` en el futuro podría "corregir" estos casos pensando que son bugs, sin saber que están documentados como comportamiento preservado en este spec.
  *Mitigación:* los tests de dominio (paso 3 del plan) cubren explícitamente cada uno de estos casos, sirviendo como documentación ejecutable — un cambio accidental los haría fallar.

- **Las entities de `content` para estos 4 tipos son `EAGER`** (specs 03-06), a diferencia de `PreguntaSimpleEntity`/`VerdaderoOFalsoEntity` (spec 13) que no tienen colecciones anidadas. Cada `findById` en los adapters nuevos carga la lista completa de opciones anidadas aunque `verify` solo necesite compararlas una vez; y `save` (que relee la entity para actualizar el contador de crítico) vuelve a pagar esa misma carga completa.
  *Mitigación:* ninguna en este spec — es el mismo costo que ya paga `content` hoy con sus propias operaciones sobre estas entities EAGER (decisión ya tomada en specs 03-06); no es una regresión introducida acá, solo se hereda.

- **Eliminar el fallback añade un punto de registro más para tipos de pregunta futuros.** Antes de este spec, un tipo de pregunta no contemplado en el dispatch de `VerificarRespuestaController` caía automáticamente en `PreguntaService.verifyResponse` (que sí sabe resolver cualquier tipo registrado en su propio `mapDeRepositorios`). Después de este spec, un séptimo tipo de pregunta que no se agregue explícitamente al `Map` de `VerificarRespuestaController` lanzará `BussinesException` en `/verify`, aunque funcione en el resto de endpoints — un registro manual más que sumar a los ya documentados en `CLAUDE.md` (subclase de `Pregunta`, `FabricaDePreguntas`, `AsignadorDeTipoALasPreguntas`, repositorio nuevo, y ahora también este `Map`).
  *Mitigación:* ninguna adicional — es el costo aceptado a cambio de que un tipo no soportado falle explícito en vez de silenciosamente vía un fallback que, en la práctica, tras este spec, ya no tendría motivo de existir para ningún tipo válido actual.

- **`AResponder.tipo` en el modelo JPA viejo sigue siendo un campo mantenido a mano, no un discriminator de JPA** (mismo riesgo estructural documentado en specs anteriores) — el dispatch de `VerificarRespuestaController` confía en `tipoDePregunta` del DTO enviado por el cliente, no en el tipo real almacenado; un cliente que envíe un id real con el tipo equivocado cae en `BussinesException` de "no encontrado" del `RepositoryPort` incorrecto, igual que ya documentó spec 12 para `random-ids`.
  *Mitigación:* ninguna nueva — mismo comportamiento ya aceptado en toda la migración, no es una regresión de este spec.
