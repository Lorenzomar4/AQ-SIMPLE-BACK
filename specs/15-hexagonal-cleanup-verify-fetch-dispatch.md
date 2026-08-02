# Spec 15 — Limpieza de código muerto: fallback legacy y dispatch por `Map` en `PreguntaController`/`VerificarRespuestaController`

**Estado:** Implementado
**Dependencias:** Spec 14 (deja los 6 tipos de pregunta cubiertos en `POST /questions/verify`), specs 11/12 y el commit `16f62f8` (dejan los 6 tipos cubiertos en `fetch`/`fetch-full`/`POST /questions`/`PUT /questions`). Este spec no agrega comportamiento nuevo — solo remueve caminos que quedaron inalcanzables como consecuencia de esos specs.
**Fecha:** 2026-08-02
**Objetivo:** Con los 6 tipos de pregunta (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO`, `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`) ya cubiertos por handlers hexagonales en los 5 endpoints de pregunta (`fetch`, `fetch-full`, `POST /questions`, `PUT /questions`, `POST /questions/verify`), las ramas `else` que caían a `PreguntaService` (viejo) quedaron sin ningún caller productivo posible — los únicos `TipoAResponder` que podrían activarlas (`CUESTIONARIO`, `TEMA`, `SUBTEMA`) nunca deberían llegar a un endpoint de pregunta. Este spec: (1) elimina esas ramas y reemplaza el fallback silencioso por un `BussinesException` explícito; (2) de paso, migra el dispatch de `if/else` a `Map<TipoAResponder, Function<...>>`, siguiendo el mismo patrón ya usado en `EliminarPreguntaPorIdService`/`ObtenerIdsAleatoriosDePreguntasService` (specs 09/12); (3) borra el código de `Service/PreguntaService`/`Controller/TemarioController` que queda huérfano como consecuencia directa de (1).

---

## Alcance

### Incluido

- **`PreguntaController` (modificado):**
  - `POST /questions/fetch` (`getQuestion`) y `POST /questions/fetch-full` (`getQuestionFull`): reemplazan el `if/else` de 7 ramas (6 tipos + fallback) por `Map<TipoAResponder, Function<Long, Object>>` construido en `@PostConstruct`. Tipo no encontrado en el mapa → `BussinesException`.
  - `POST /questions` (`createQuestion`): mismo patrón, `Map<TipoAResponder, Function<PostPreguntaDTO, CreateQuestionResponseDTO>>`.
  - `PUT /questions` (`updateQuestion`): mismo patrón, `Map<TipoAResponder, Function<PostPreguntaDTO, Pregunta>>`. Cada rama actual (que edita vía el `UseCase` de `content` y arma a mano el objeto de vista vieja `@JsonView` — `PreguntaSimple`, `VerdaderoOFalso`, `SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`) se extrae tal cual a un método privado (`editarPreguntaSimple`, `editarVerdaderoOFalso`, etc.), sin cambiar su lógica interna — solo se mueve de un bloque `if` a un método referenciado desde el `Map`.
  - Se elimina el campo `PreguntaService preguntaService` (y su import) — sin caller en este archivo tras el cambio.
- **`VerificarRespuestaController` (modificado):** mismo patrón — `if/else` de 7 ramas → `Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>>`. Cada rama (algunas con mapeo de listas del DTO viejo a los DTOs de comando de `answering`) se extrae a un método privado. Se elimina el campo `PreguntaService preguntaService`, su parámetro de constructor y su import.
- **`TemarioController` (modificado):** se elimina el campo `@Autowired PreguntaService preguntaService` — no tiene ningún uso en este archivo (verificado: ningún método lo invoca).
- **`Service/PreguntaService` (modificado):** se elimina el método `obtenerPreguntaFull(ObtenerPreguntaDTO)` — su único caller era la rama muerta de `PreguntaController.getQuestionFull`; no lo usa ningún test (a diferencia de `obtenerPregunta`, `createaQuestion`, `updateQuestion`, `verifyResponse`, que siguen siendo baseline de tests de paridad y se mantienen intactos).
- **Tests:** ningún test nuevo — este spec no cambia comportamiento observable, solo la forma del dispatch y elimina caminos inalcanzables. La batería de tests de paridad existente (specs 01-14) debe seguir pasando sin modificaciones, sirviendo como prueba de que el refactor no alteró ningún resultado.

### Explícitamente NO incluido

- **No se toca `Service/PreguntaService.obtenerPregunta`, `.createaQuestion`, `.updateQuestion`, `.verifyResponse`, `.createInverseQuestion`, `.delete`, `.getIssueItems`, `.getListOfPreguntaSimples`** — todos siguen siendo baseline activo de tests de paridad en `src/test/java/com/lorenzomar3/AQ/content/*` y `src/test/java/com/lorenzomar3/AQ/answering/*`. Borrarlos rompería esos tests sin ninguna ganancia real (siguen siendo la única forma de comparar "camino viejo" vs. "camino nuevo").
- **No se tocan `Service/TemarioService` ni `Service/ResponderService`** — mismo motivo: son baseline de `ResponderParidadTest`, `IssueItemsParidadTest`, `IssueQuestionIdsInverseParidadTest`, `IssueParidadTest`. Aunque no tienen ningún caller productivo (ninguna clase de producción los invoca fuera de sí mismos), no son código muerto en sentido estricto — siguen ejecutándose activamente desde los tests.
- **No se agregan tests nuevos de comportamientos raros** para los 4 tipos de spec 14 (deuda ya documentada en ese spec) — está fuera de alcance de este spec, que es puramente estructural.
- **No se relocan los controllers** de `Controller/` a `content/infrastructure/controller/`/`answering/infrastructure/controller/` — deuda documentada, spec futuro aparte.
- **No se tocan `/questions/inverse` ni `fetch`/`fetch-full`** más allá de cambiar la forma del dispatch — su lógica interna (qué `UseCase`/`QueryHandler` se invoca por tipo) no cambia.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

No hay cambios de modelo de datos — este spec es puramente estructural sobre controllers y un método de servicio. El único patrón nuevo introducido es el `Map<TipoAResponder, Function<...>>` + `@PostConstruct`, ya establecido en el codebase:

```java
private final Map<TipoAResponder, Function<Long, Object>> mapDeObtencion = new HashMap<>();

@PostConstruct
private void init() {
    mapDeObtencion.put(TipoAResponder.PREGUNTA_SIMPLE, obtenerPreguntaUseCase::obtener);
    mapDeObtencion.put(TipoAResponder.VERDADERO_FALSO, obtenerVerdaderoOFalsoUseCase::obtener);
    mapDeObtencion.put(TipoAResponder.SELECCION_UNICA, id -> obtenerSeleccionUnicaQueryHandler.handle(new ObtenerSeleccionUnicaQuery(id)));
    // ... resto de los 6 tipos
}

@PostMapping("/questions/fetch")
public ResponseEntity<Object> getQuestion(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
    logger.info("[POST /questions/fetch] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipoAResponder());
    Function<Long, Object> obtener = mapDeObtencion.get(getQuestionDTO.tipoAResponder());
    if (obtener == null) {
        throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
    }
    return new ResponseEntity<>(obtener.apply(getQuestionDTO.id()), HttpStatus.OK);
}
```

Mismo esqueleto para `fetch-full` (`mapDeObtencionFull`), `POST /questions` (`mapDeCreacion`, `Function<PostPreguntaDTO, CreateQuestionResponseDTO>`), `PUT /questions` (`mapDeEdicion`, `Function<PostPreguntaDTO, Pregunta>`, con los 6 métodos privados de edición) y `POST /questions/verify` (`mapDeVerificacion`, `Function<RespuestaDePreguntaDTO, Boolean>`, con los 6 métodos privados de verificación que arman el `Command` correspondiente de `answering`).

---

## Plan de implementación

1. **`PreguntaController` — `fetch`/`fetch-full`.** Construir `mapDeObtencion`/`mapDeObtencionFull` en un único `@PostConstruct init()`. Reemplazar los cuerpos de `getQuestion`/`getQuestionFull` por el lookup + `BussinesException`. Quitar el import de `MappingJacksonValue`/`View` de esas dos ramas si queda sin uso fuera de `updateQuestion` (`View` se mantiene por el `@JsonView` de `updateQuestion`).
2. **`PreguntaController` — `POST /questions`.** Construir `mapDeCreacion`. Reemplazar el cuerpo de `createQuestion`.
3. **`PreguntaController` — `PUT /questions`.** Extraer cada rama actual a un método privado (`editarPreguntaSimple`, `editarVerdaderoOFalso`, `editarSeleccionUnica`, `editarOpcionMultiple`, `editarDesplegableCompartido`, `editarDesplegableIndependiente`), cada uno devolviendo `Pregunta` (superclase común de las 6 clases de vista vieja). Construir `mapDeEdicion`. Reemplazar el cuerpo de `updateQuestion`.
4. **`PreguntaController` — limpieza final.** Quitar el campo `PreguntaService preguntaService` y su import; agregar el import de `BussinesException` si no estaba.
5. **`VerificarRespuestaController`.** Extraer cada rama a un método privado (`verificarPreguntaSimple`, `verificarVerdaderoOFalso`, `verificarSeleccionUnica`, `verificarOpcionMultiple`, `verificarDesplegableCompartido`, `verificarDesplegableIndependiente`). Construir `mapDeVerificacion` en `@PostConstruct`. Reemplazar el cuerpo de `verifyRequestForUser`. Quitar el campo/parámetro de constructor `PreguntaService preguntaService` y su import.
6. **`TemarioController`.** Quitar el campo `PreguntaService preguntaService` y su import.
7. **`Service/PreguntaService`.** Borrar el método `obtenerPreguntaFull`.
8. **Verificación.** Correr `./mvnw test` (lo corre el usuario) — la batería completa de tests de paridad (specs 01-14) debe seguir pasando sin cambios, confirmando que el refactor no alteró ningún resultado observable. Probar manualmente contra `AQ-SIMPLE-FRONT`: `fetch`/`fetch-full`/crear/editar/verificar una pregunta de cada uno de los 6 tipos.

---

## Criterios de aceptación

- [x] `PreguntaController.getQuestion`/`getQuestionFull`/`createQuestion`/`updateQuestion` despachan vía `Map<TipoAResponder, Function<...>>`; ningún `if/else` por tipo permanece en esos 4 métodos.
- [x] `VerificarRespuestaController.verifyRequestForUser` despacha vía `Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>>`.
- [x] Ningún tipo no soportado cae silenciosamente a `PreguntaService` — los 4 métodos de `PreguntaController` y el de `VerificarRespuestaController` lanzan `BussinesException` para un tipo ausente del `Map`.
- [x] `PreguntaController` y `VerificarRespuestaController` ya no inyectan `PreguntaService` en ningún punto.
- [x] `TemarioController` ya no inyecta `PreguntaService`.
- [x] `Service/PreguntaService` ya no tiene el método `obtenerPreguntaFull`.
- [x] `Service/PreguntaService.obtenerPregunta`, `.createaQuestion`, `.updateQuestion`, `.verifyResponse`, `.createInverseQuestion`, `.delete`, `.getIssueItems`, `.getListOfPreguntaSimples` siguen existiendo sin cambios (siguen siendo baseline de tests).
- [x] `Service/TemarioService` y `Service/ResponderService` no fueron tocados.
- [x] El comportamiento observable de los 5 endpoints (`fetch`, `fetch-full`, `POST /questions`, `PUT /questions`, `POST /questions/verify`) para los 6 tipos de pregunta es idéntico al de antes del refactor — mismo JSON de respuesta, mismos códigos HTTP (cada rama vieja se movió tal cual a un método privado/lambda, sin tocar su lógica interna).
- [ ] `./mvnw test` corre completo y pasa, sin modificar ningún test existente — **pendiente, lo corre el usuario.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT` de los 6 tipos en los 5 endpoints — **pendiente, lo corre el usuario.**

---

## Decisiones tomadas

- **Se migra el dispatch a `Map<TipoAResponder, Function<...>>` en el mismo spec que elimina el fallback, en vez de separarlos en dos specs.** Son cambios acoplados: no tiene sentido dejar un `if/else` de 6 ramas + un `BussinesException` colgado al final sin el `Map`, cuando ese es exactamente el patrón que el resto del codebase ya usa para este caso (specs 09/12) y que el propio borrador de spec 14 pedía.
- **No se tocan `PreguntaService`/`TemarioService`/`ResponderService` como clases** — a diferencia de lo que "limpieza de código muerto" podría sugerir a primera vista, estas clases siguen vivas como baseline de la batería de tests de paridad que sostiene toda la migración desde spec 01. Borrarlas (o cualquiera de sus métodos usados en tests) rompería esa red de contención sin ninguna ganancia — el objetivo de este spec es sacar código *inalcanzable en producción*, no reescribir la estrategia de testing de paridad.
- **`obtenerPreguntaFull` sí se borra**, porque es el único método de `PreguntaService` que queda sin ningún caller (ni producción ni test) tras este spec — es la excepción que confirma la regla anterior.
- **Los 6 métodos privados de `updateQuestion` (edición) y `verifyRequestForUser` (verificación) se extraen tal cual, sin tocar su lógica interna** — este spec es un refactor de forma (dónde vive el dispatch), no de comportamiento. Cualquier cambio de lógica dentro de esas ramas queda fuera de alcance.

---

## Riesgos identificados

- **El refactor toca los 5 endpoints más usados de la API en un solo spec.** Aunque el cambio es mecánico (mover código de un `if` a un método + una entrada de `Map`), el área de superficie es grande. *Mitigación:* la batería de tests de paridad existente (specs 01-14) ya cubre los 6 tipos en los 5 endpoints con comparación explícita contra el camino viejo — si el refactor introduce una regresión, debería fallar ahí antes de llegar a manual testing.
- **`BussinesException` en vez de fallback silencioso es un cambio de comportamiento real para el caso borde "tipo no registrado".** Antes, un tipo no contemplado cliente-side cae a `PreguntaService` (que sabe resolver cualquier tipo en su propio `mapDeRepositorios`, incluyendo teóricamente tipos contenedor si alguien los mandara por error). Después de este spec, ese mismo caso lanza `BussinesException` de inmediato. *Mitigación:* ninguna nueva — mismo argumento ya aceptado en spec 14 para `/verify`: los 3 tipos contenedor nunca deberían llegar a estos endpoints, así que en la práctica no hay ningún caller legítimo que dependa del fallback.
- **Compromiso de larga duración:** un séptimo tipo de pregunta futuro que no se agregue a los 5 `Map` nuevos (`mapDeObtencion`, `mapDeObtencionFull`, `mapDeCreacion`, `mapDeEdicion`, `mapDeVerificacion`) fallará explícito con `BussinesException` en vez de funcionar parcialmente vía el viejo `PreguntaService`. Es el mismo trade-off ya aceptado en spec 14, ahora extendido a los otros 4 endpoints.
