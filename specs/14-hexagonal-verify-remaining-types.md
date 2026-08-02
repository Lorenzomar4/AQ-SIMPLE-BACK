# Spec 14 — Migración hexagonal: `POST /questions/verify` para los 4 tipos restantes

**Estado:** Implementado (documentado retroactivamente — ver [Historia de este documento](#historia-de-este-documento))
**Dependencias:** Spec 13 (`answering/` slice, `EstadoCritico`, `VerificarRespuestaController`). Specs 03-06 (`SeleccionUnicaRepositoryPort`, `OpcionMultipleRepositoryPort`, `DesplegableCompartidoRepositoryPort`, `DesplegableIndependienteRepositoryPort` y sus `Entity`/`JpaRepository` de `content`) — pero, a diferencia de lo planeado originalmente, `answering` ya no los toca directo: los consume indirectamente a través de `content.api`.
**Fecha:** 2026-08-01 (borrador) — 2026-08-02 (implementación real, commit `16f62f8` "continuacion de la migracion")
**Objetivo:** Migrar `POST /questions/verify` a arquitectura hexagonal para los 4 tipos de pregunta restantes (`SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`), completando el dispatch de `VerificarRespuestaController` para los 6 tipos de pregunta.

---

## Historia de este documento

La primera versión de este spec (borrador, 2026-08-01) proponía extender el patrón exacto de spec 13: `port/in/XxxUseCase` + `application/service/XxxService` en `answering`, con adapters que inyectan **directo** los `JpaRepository` de `content` (specs 03-06), y reescribir inline en cada objeto de dominio la lógica de comparación sin extraer ningún helper compartido.

Lo que se construyó realmente (commit `16f62f8`, un día después) diverge del borrador en varios puntos de diseño, y además fue más allá de su alcance declarado. Se reescribe este documento para reflejar el código tal como quedó, no como se planeó. Las diferencias:

1. **Cambio de patrón de nomenclatura, y con retroefecto sobre spec 13.** El borrador proponía `UseCase`/`Service` (igual que `content/`). El código real usa `Command`/`Handler` en todo `answering/application/command/` — y de paso migró `VerificarRespuestaPreguntaSimpleUseCase`/`Service` y `VerificarRespuestaVerdaderoOFalsoUseCase`/`Service` (de spec 13) al mismo patrón, alineando finalmente `answering` con la nomenclatura que `ARQUITECTURAV3.md` documenta como estándar del slice.
2. **Cambio de arquitectura cross-slice — el más significativo.** El borrador (y spec 13 antes) tenían a `answering` inyectando **directo** los `JpaRepository` de `content` (specs 01-06) desde sus propios adapters, leyendo `Entity` de otro slice. El código real elimina esa deuda: `answering` ya no importa nada de `content.infrastructure` ni `content.domain`. En su lugar, `content` expone en `content.api` un `Obtener...ParaResponderQuery` (lectura) y un `ActualizarCritico...Command` (escritura) por cada uno de los 6 tipos; `content.application` los implementa; `answering.application.command` los consume como colaboradores externos. Esto es exactamente el patrón "Entre slices" de `ARQUITECTURAV3.md` (`answering` solo conoce `content.api`) y resuelve por completo el riesgo que el borrador de este spec documentaba y aceptaba sin mitigación ("`JpaRepository` usado por dos adapters de dos slices distintos"). Como consecuencia, `PreguntaSimpleParaResponderAdapter` y `VerdaderoOFalsoParaResponderAdapter` (los dos adapters de spec 13) se borraron.
3. **Se extrajo exactamente el helper genérico que el borrador decidía no extraer.** La sección "Decisiones tomadas y descartadas" del borrador rechazaba explícitamente un `Verificador<T,G>` compartido. El código real tiene `answering/domain/VerificadorDeOpciones<T, O extends OpcionVerificable<T>>` + la interfaz `OpcionVerificable<T>`, usado por los 4 objetos `...ParaResponder` nuevos. Ver [Decisiones tomadas](#decisiones-tomadas-reales) para la justificación reconstruida.
4. **Alcance ampliado sin nuevo spec.** El mismo commit migró también `POST /questions/inverse` (`CrearPreguntaInversaCommand`/`Handler`) y `fetch`/`fetch-full` para los 4 tipos nuevos (`Obtener...QueryHandler`/`Obtener...FullQueryHandler` en `content.application.query`) — ambos explícitamente fuera de alcance en el borrador. Esos dos bloques de trabajo no están cubiertos por ningún spec numerado y quedan pendientes de documentar aparte; este documento **no** los cubre, solo los menciona como contexto.
5. **El dispatch del controller no cambió de forma.** El borrador pedía reemplazar el `if/else` por un `Map<TipoAResponder, Function<...>>` en `@PostConstruct` y eliminar la inyección de `PreguntaService`. El código real extendió el `if/else` existente a 6 ramas y **mantiene** el campo `preguntaService` y su rama `else` de fallback (hoy inalcanzable en la práctica, pero no removida).
6. **Cobertura de tests menor a la exigida por el borrador.** Ver [Deuda de testing](#deuda-de-testing-pendiente).

---

## Alcance

### Incluido (lo que realmente existe)

- **`answering/domain/` (Java puro, sin anotaciones de framework, sin imports de `content.domain`/`model.AResponder.*`):**
  - `OpcionVerificable<T>` — interfaz con `Long getId()` y `T getValorCorrecto()`.
  - `VerificadorDeOpciones<T, O extends OpcionVerificable<T>>` — helper genérico, único método `boolean coincidenciaTotal(List<O> opcionesReales, List<O> opcionesDelUsuario)`: arma un `Map<Long, T>` desde `opcionesReales` y hace `allMatch` de `opcionesDelUsuario` contra ese mapa. `real.get(id)` sin chequeo de nulidad — si el usuario manda un `id` que no existe en `opcionesReales`, `.equals(...)` sobre `null` lanza `NullPointerException` (comportamiento heredado del `Verificador` viejo, preservado sin comentario explicativo).
  - `OpcionParaResponder(Long id, Boolean esCorrecta)` — record, implementa `OpcionVerificable<Boolean>`. Usado por `SeleccionUnicaParaResponder` y `OpcionMultipleParaResponder`.
  - `OpcionDeDesplegableCompartidoParaResponder(Long id, String respuesta)` — record, implementa `OpcionVerificable<String>`.
  - `SubPreguntaParaResponder(Long id, List<OpcionParaResponder> opciones)` — record, implementa `OpcionVerificable<Long>`. `getValorCorrecto()` hace `opciones.stream().filter(OpcionParaResponder::esCorrecta).toList().get(0).id()` — mismo `.get(0)` sin chequeo del modelo viejo (`IndexOutOfBoundsException` si ninguna opción está marcada correcta).
  - `SeleccionUnicaParaResponder` — campos `Long id`, `List<OpcionParaResponder> opciones`, `EstadoCritico estadoCritico`. `verificarRespuesta(List<OpcionParaResponder>)`: valida cardinalidad (`BussinesException` si la respuesta del usuario no tiene exactamente una opción con `esCorrecta = true`), delega la comparación en `VerificadorDeOpciones`, actualiza `estadoCritico`.
  - `OpcionMultipleParaResponder` — mismos campos, **sin** validación de cardinalidad; delega directo en `VerificadorDeOpciones` (lista vacía del usuario → `allMatch` sobre vacío → `true`).
  - `DesplegableCompartidoParaResponder` — campos `Long id`, `List<OpcionDeDesplegableCompartidoParaResponder> opciones`, `EstadoCritico estadoCritico`; usa `VerificadorDeOpciones<String, ...>`.
  - `DesplegableIndependienteParaResponder` — campos `Long id`, `List<SubPreguntaParaResponder> subPreguntas`, `EstadoCritico estadoCritico`; usa `VerificadorDeOpciones<Long, ...>` (compara, por cada sub-pregunta, el id de opción que el usuario marcó contra el id de la opción realmente correcta).
  - `EstadoCritico` — sin cambios respecto a spec 13.

- **`content/api/` (nuevo, contratos públicos del slice `content` — no de `answering`):**
  - `ObtenerSeleccionUnicaParaResponderQuery`, `ObtenerOpcionMultipleParaResponderQuery`, `ObtenerDesplegableCompartidoParaResponderQuery`, `ObtenerDesplegableIndependienteParaResponderQuery` — cada uno `Optional<XxxParaResponderView> obtenerPorId(Long id)`.
  - `ActualizarCriticoDeSeleccionUnicaCommand`, `ActualizarCriticoDeOpcionMultipleCommand`, `ActualizarCriticoDeDesplegableCompartidoCommand`, `ActualizarCriticoDeDesplegableIndependienteCommand` — cada uno `void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible)`.
  - Views: `SeleccionUnicaParaResponderView(Long id, List<OpcionView> listaDeOpciones, Integer intentosParaQueDejeDeSerCriticoDisponible)`, `OpcionMultipleParaResponderView` (misma forma), `DesplegableCompartidoParaResponderView(Long id, List<OpcionDeDesplegableCompartidoView> listaDeOpciones, Integer intentos...)`, `DesplegableIndependienteParaResponderView(Long id, List<SubPreguntaView> listaDeOpciones, Integer intentos...)`, `OpcionView(Long id, Boolean laRespuestaEs)`, `OpcionDeDesplegableCompartidoView(Long id, String respuesta)`, `SubPreguntaView(Long id, List<OpcionView> opciones)`.
  - **Retrofit de spec 13:** también se agregaron `ObtenerPreguntaSimpleParaResponderQuery`/`ObtenerVerdaderoOFalsoParaResponderQuery`, `ActualizarCriticoDePreguntaSimpleCommand`/`ActualizarCriticoDeVerdaderoOFalsoCommand` y sus Views — `PreguntaSimple`/`VerdaderoOFalso` pasaron a consumir `content.api` igual que los 4 tipos nuevos, dejando de usar el adapter cross-slice directo de spec 13.

- **`content/application/query/` y `content/application/command/` (nuevo):** `Obtener...ParaResponderHandler` (implementa el `Query` de `api/`, inyecta el `XxxRepositoryPort` correspondiente de specs 01-06, mapea `content.domain` → `content.api.XxxView`) y `ActualizarCritico...Handler` (implementa el `Command` de `api/`, `findById` con `BussinesException` si no existe, setea el contador, `save`) — uno de cada por los 6 tipos.

- **`answering/application/command/` (nuevo, patrón `Command`/`Handler`):**
  - DTOs de comando: `OpcionRespuestaDTO(Long id, Boolean marcada)`, `OpcionDeDesplegableCompartidoRespuestaDTO(Long id, String respuesta)`, `SubPreguntaRespuestaDTO(Long id, List<OpcionRespuestaDTO> opciones)`.
  - `VerificarRespuestaSeleccionUnicaCommand(Long idPregunta, List<OpcionRespuestaDTO> opcionesDelUsuario)` + `Handler` — inyecta `ObtenerSeleccionUnicaParaResponderQuery` y `ActualizarCriticoDeSeleccionUnicaCommand` (ambos de `content.api`, ningún import de `content.domain`/`content.infrastructure`); resuelve la vista (`BussinesException` si no existe), mapea a dominio de `answering`, llama `verificarRespuesta`, persiste el contador vía el `Command` de `content.api`, devuelve el booleano.
  - `VerificarRespuestaOpcionMultipleCommand`/`Handler`, `VerificarRespuestaDesplegableCompartidoCommand`/`Handler`, `VerificarRespuestaDesplegableIndependienteCommand`/`Handler` — mismo patrón exacto, cada uno contra su par de interfaces de `content.api`.
  - **Retrofit de spec 13:** `VerificarRespuestaPreguntaSimpleHandler`/`VerificarRespuestaVerdaderoOFalsoHandler` reescritos con el mismo patrón (antes usaban `PreguntaSimpleParaResponderPort`/adapter propio; ahora usan `content.api`).

- **`answering/infrastructure/controller/VerificarRespuestaController` (modificado):** el `if/else` original de spec 13 (2 ramas + fallback) se extendió a 6 ramas + fallback, una por cada `TipoAResponder` hoja. Sigue inyectando `PreguntaService` (viejo) y su rama `else` sigue presente — no se migró al `Map<TipoAResponder, Function<...>>` que proponía el borrador, y el fallback no se eliminó pese a que, con los 6 tipos cubiertos, ya no tiene ningún caller válido posible.

- **Tests:**
  - `VerificarRespuestaPreguntaSimpleParidadTest` / `VerificarRespuestaVerdaderoOFalsoParidadTest` (retrofit de spec 13): correcta, incorrecta, contador de crítico en ambos sentidos, not-found (`BussinesException`) — vía el `Handler` directo, sin pasar por HTTP.
  - `VerificarRespuestaTiposNoMigradosNoRegresionTest` (nuevo, para los 4 tipos de este spec): un único caso "camino feliz" por tipo, comparando el resultado del `Handler` viejo (`PreguntaService.verifyResponse`, invocado directo) contra el controller nuevo (vía `MockMvc`, `POST /questions/verify` real). **No cubre** casos incorrectos, not-found, ni ninguno de los comportamientos raros.
  - `VerificadorDeOpcionesTest` (nuevo): 3 casos sobre el helper genérico en aislamiento (coincidencia total, no-coincidencia, coincidencia parcial con subconjunto del usuario). No cubre lista vacía, id inexistente (`NullPointerException`) ni el `IndexOutOfBoundsException` de `SubPreguntaParaResponder.getValorCorrecto()`.

### Explícitamente NO incluido (ni en el borrador, ni en lo construido)

- Corregir los comportamientos raros preservados (cardinalidad, listas vacías = correctas, `NullPointerException`, `IndexOutOfBoundsException`) — se mantienen intencionalmente.
- Cambiar `RespuestaDePreguntaDTO` ni las clases viejas del DTO que referencia.
- `Service.PreguntaService.verifyResponse` (viejo) no se elimina — sigue inyectado en `VerificarRespuestaController` como fallback (a diferencia del borrador, que pedía quitarlo) y sigue siendo el baseline de los tests de paridad.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

### Entregado fuera de alcance, en el mismo commit, sin spec propio

- `POST /questions/inverse` (`content/application/command/CrearPreguntaInversaCommand`/`Handler`), con su propio test de paridad (`QuestionInverseParidadTest`).
- `POST /questions/fetch` y `/fetch-full` para `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE` (`Obtener...QueryHandler`/`Obtener...FullQueryHandler` en `content.application.query`, cableados en `PreguntaController`).

Ninguno de los dos está documentado en un spec numerado. Se recomienda escribir specs 15/16 retroactivos para ellos con el mismo criterio aplicado acá, antes de seguir construyendo sobre esa base sin registro.

---

## Modelo de datos

### Dominio nuevo — `answering/domain/`

```java
public interface OpcionVerificable<T> {
    Long getId();
    T getValorCorrecto();
}

public class VerificadorDeOpciones<T, O extends OpcionVerificable<T>> {
    public boolean coincidenciaTotal(List<O> opcionesReales, List<O> opcionesDelUsuario) {
        Map<Long, T> valorCorrectoPorId = new HashMap<>();
        opcionesReales.forEach(o -> valorCorrectoPorId.put(o.getId(), o.getValorCorrecto()));
        return opcionesDelUsuario.stream()
                .allMatch(o -> valorCorrectoPorId.get(o.getId()).equals(o.getValorCorrecto()));
    }
}

public record OpcionParaResponder(Long id, Boolean esCorrecta) implements OpcionVerificable<Boolean> { ... }
public record OpcionDeDesplegableCompartidoParaResponder(Long id, String respuesta) implements OpcionVerificable<String> { ... }
public record SubPreguntaParaResponder(Long id, List<OpcionParaResponder> opciones) implements OpcionVerificable<Long> {
    @Override
    public Long getValorCorrecto() {
        return opciones.stream().filter(OpcionParaResponder::esCorrecta).toList().get(0).id();
    }
}

public class SeleccionUnicaParaResponder {
    private Long id;
    private List<OpcionParaResponder> opciones;
    private EstadoCritico estadoCritico;
    private final VerificadorDeOpciones<Boolean, OpcionParaResponder> verificador = new VerificadorDeOpciones<>();

    public Boolean verificarRespuesta(List<OpcionParaResponder> opcionesDelUsuario) {
        long marcadas = opcionesDelUsuario.stream().filter(OpcionParaResponder::esCorrecta).count();
        if (marcadas != 1) throw new BussinesException("¡Asegurese de que haya solamente una opcion valida!");
        boolean esCorrecta = verificador.coincidenciaTotal(opciones, opcionesDelUsuario);
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
// OpcionMultipleParaResponder, DesplegableCompartidoParaResponder, DesplegableIndependienteParaResponder:
// mismo esqueleto que SeleccionUnicaParaResponder, sin la validación de cardinalidad.
```

### `content/api/` (contratos consumidos por `answering`, uno por tipo, patrón repetido 6 veces)

```java
public interface ObtenerSeleccionUnicaParaResponderQuery {
    Optional<SeleccionUnicaParaResponderView> obtenerPorId(Long id);
}
public record SeleccionUnicaParaResponderView(Long id, List<OpcionView> listaDeOpciones,
                                               Integer intentosParaQueDejeDeSerCriticoDisponible) {}

public interface ActualizarCriticoDeSeleccionUnicaCommand {
    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
```

Implementados en `content.application.query.ObtenerSeleccionUnicaParaResponderHandler` / `content.application.command.ActualizarCriticoDeSeleccionUnicaHandler`, inyectando el `SeleccionUnicaRepositoryPort` ya existente (spec 03) — `answering` no ve ese port ni el `RepositoryPort`/`Entity` detrás.

### `answering/application/command/`

```java
public record VerificarRespuestaSeleccionUnicaCommand(Long idPregunta, List<OpcionRespuestaDTO> opcionesDelUsuario) {}

@Service
public class VerificarRespuestaSeleccionUnicaHandler {
    private final ObtenerSeleccionUnicaParaResponderQuery obtenerQuery;
    private final ActualizarCriticoDeSeleccionUnicaCommand actualizarCritico;

    public Boolean ejecutar(VerificarRespuestaSeleccionUnicaCommand command) {
        SeleccionUnicaParaResponderView view = obtenerQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));
        SeleccionUnicaParaResponder pregunta = toDomain(view);
        Boolean esCorrecta = pregunta.verificarRespuesta(mapearOpciones(command.opcionesDelUsuario()));
        actualizarCritico.actualizar(pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }
}
```

### Reutilizado tal cual (sin cambios)

- `com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO` — DTO de request, sin cambios.
- `com.lorenzomar3.AQ.exception.BussinesException`, `com.lorenzomar3.AQ.model.TipoAResponder`.
- `Service.PreguntaService.verifyResponse` (viejo) — baseline de tests de paridad y fallback aún presente (no eliminado) en el controller.
- `content.application.port.out.SeleccionUnicaRepositoryPort`/`OpcionMultipleRepositoryPort`/`DesplegableCompartidoRepositoryPort`/`DesplegableIndependienteRepositoryPort` (specs 03-06) — siguen viviendo en `content`, ahora usados solo desde dentro de `content` (los nuevos `Obtener...Handler`/`ActualizarCritico...Handler`), nunca desde `answering` directo.

---

## Deuda de testing pendiente

A diferencia de spec 13 (que cubrió `PreguntaSimple`/`VerdaderoOFalso` con tests unitarios de dominio, paridad correcta/incorrecta, not-found, y actualización de crítico en ambos sentidos), los 4 tipos de este spec quedaron con cobertura parcial:

- [ ] No hay tests unitarios de dominio para `SeleccionUnicaParaResponder`, `OpcionMultipleParaResponder`, `DesplegableCompartidoParaResponder`, `DesplegableIndependienteParaResponder` en aislamiento.
- [ ] No hay test de la validación de cardinalidad de `SeleccionUnicaParaResponder` (0 o ≥2 opciones marcadas por el usuario → `BussinesException`).
- [ ] No hay test de lista vacía del usuario = "correcta" para `OpcionMultipleParaResponder`/`DesplegableCompartidoParaResponder`/`DesplegableIndependienteParaResponder`.
- [ ] No hay test de `NullPointerException` por id inexistente en la respuesta del usuario, para ninguno de los 4 tipos.
- [ ] No hay test de `IndexOutOfBoundsException` en `SubPreguntaParaResponder.getValorCorrecto()` cuando ninguna opción real está marcada correcta.
- [ ] No hay test de respuesta **incorrecta** (solo "camino feliz" correcto) para los 4 tipos.
- [ ] No hay test de not-found (`BussinesException`) para los 4 tipos nuevos — sí existe para `PreguntaSimple`/`VerdaderoOFalso`.
- [x] `VerificadorDeOpciones` (el helper compartido) sí tiene tests unitarios propios, aunque solo cubren coincidencia total/parcial/nula — no lista vacía ni id inexistente.

Estos casos son precisamente los que spec 13 usó como red de contención para comportamiento preservado deliberadamente raro; sin ellos, un cambio futuro en `VerificadorDeOpciones` o en cualquiera de los 4 objetos de dominio puede alterar ese comportamiento sin que ningún test lo detecte.

---

## Criterios de aceptación

- [x] Existen `OpcionParaResponder`, `OpcionDeDesplegableCompartidoParaResponder`, `SubPreguntaParaResponder`, `SeleccionUnicaParaResponder`, `OpcionMultipleParaResponder`, `DesplegableCompartidoParaResponder`, `DesplegableIndependienteParaResponder` en `answering/domain/`, Java puro.
- [x] `SeleccionUnicaParaResponder.verificarRespuesta` lanza `BussinesException` si la respuesta del usuario no tiene exactamente una opción correcta.
- [x] `OpcionMultipleParaResponder`/`DesplegableCompartidoParaResponder`/`DesplegableIndependienteParaResponder.verificarRespuesta` no validan cardinalidad; lista vacía del usuario se evalúa como correcta.
- [x] Los 4 objetos lanzan `NullPointerException` sin atrapar cuando la respuesta del usuario referencia un id inexistente (heredado de `VerificadorDeOpciones`).
- [x] `EstadoCritico.actualizar` (sin modificar desde spec 13) se invoca desde los 4 objetos nuevos.
- [x] `POST /questions/verify` para cada uno de los 4 tipos nuevos devuelve el mismo booleano que el camino viejo, verificado para el caso correcto (`VerificarRespuestaTiposNoMigradosNoRegresionTest`).
- [ ] ~~`POST /questions/verify` con un id inexistente de cualquiera de los 4 tipos nuevos lanza `BussinesException`~~ — no tiene test dedicado para los 4 tipos nuevos (sí para `PreguntaSimple`/`VerdaderoOFalso`), aunque el comportamiento del código (`orElseThrow` en el `Handler`) lo garantiza.
- [x] `VerificarRespuestaController` despacha los 6 tipos de pregunta a su `Handler` correspondiente.
- [ ] ~~Ya no inyecta `PreguntaService` ni tiene rama de fallback~~ — **no se cumplió**: el campo y la rama siguen presentes.
- [ ] ~~El dispatch usa `Map<TipoAResponder, Function<...>>`~~ — **no se cumplió**: sigue siendo `if/else`.
- [x] Ningún archivo bajo `answering/` importa `content.application`, `content.domain`, `content.infrastructure` ni `model.AResponder.*` — mejora respecto al borrador original: ni siquiera se reutiliza el `JpaRepository`/`Entity` de `content` desde infraestructura, todo pasa por `content.api`.
- [ ] Tests cubriendo comportamientos raros de los 4 tipos nuevos — **no se cumplió**, ver [Deuda de testing pendiente](#deuda-de-testing-pendiente).
- [x] `./mvnw test` corre completo y pasa — verificado por el usuario (trabajo ya en `hexagonal-dev`, working tree limpio).
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT` — no confirmada en esta sesión.
- [x] `PreguntaService.verifyResponse` (viejo) no fue eliminado; sigue como fallback activo en el controller y baseline de tests de paridad.

---

## Decisiones tomadas (reales)

- **Se descartó la idea original de reutilizar directo el `JpaRepository` de `content` desde `answering`, a favor de `content.api`.** Esto contradice tanto el borrador de este spec como spec 13 (que sí hicieron reuso directo, aceptando el riesgo "dos slices comparten un `JpaRepository`"). El cambio real resuelve ese riesgo de raíz: `answering` deja de conocer cualquier detalle de persistencia de `content`. Como efecto colateral, esto también significa que `content` ahora asume el costo de mantener 12 interfaces nuevas en `api/` (6 `Query` + 6 `Command`) y sus implementaciones — más superficie pública, pero acoplamiento real más bajo.
- **Se extrajo `VerificadorDeOpciones<T, O>`, revirtiendo la decisión explícita del borrador de no hacerlo.** El borrador argumentaba que cada tipo compara "un valor de naturaleza distinta" y que una abstracción compartida sería prematura (mismo criterio que specs 10/12 con `AResponderChildRef`). En la práctica, los 4 algoritmos son estructuralmente idénticos (mapa `id → valor esperado` + `allMatch`), y `OpcionVerificable<T>` los unifica sin forzar ningún tipo a exponer algo que no le pertenece naturalmente. La decisión tomada realmente prioriza no repetir el mismo bloque de 5 líneas cuatro veces, a costa de introducir una capa de indirección (interfaz + genérico) que el borrador prefería evitar. Ninguna de las dos posturas es objetivamente correcta; se documenta acá el cambio de criterio para que quede claro que fue deliberado y no un descuido.
- **Se retrofitteó `PreguntaSimple`/`VerdaderoOFalso` (spec 13) al mismo patrón `content.api` + `Command`/`Handler`, en el mismo commit.** No estaba en el alcance de ningún spec, pero evita que el slice `answering` quede con dos patrones de acceso a datos distintos conviviendo (2 tipos con adapter directo a `JpaRepository`, 4 tipos con `content.api`) apenas un día después de haberlo escrito.
- **No se eliminó el fallback a `PreguntaService` en el controller, pese a que el borrador lo pedía.** No hay evidencia en el código de por qué se mantuvo — es la desviación menos justificable de las cinco. Queda como ítem abierto: con los 6 tipos hoja cubiertos, el campo `preguntaService` y su rama `else` en `VerificarRespuestaController` son código muerto real (ningún `TipoAResponder` container llega a `/verify`).
- **Se amplió el alcance a `/questions/inverse` y `fetch`/`fetch-full` de los 4 tipos en el mismo commit, sin abrir specs nuevos.** Práctico para no dejar el `PreguntaController` a medio migrar, pero rompe la trazabilidad spec-por-spec que el resto de la migración mantuvo consistentemente desde spec 01. Recomendación: documentar esos dos bloques retroactivamente (specs 15/16).

---

## Riesgos identificados (actualizados)

- **Cobertura de tests insuficiente para los comportamientos raros preservados** (ver [Deuda de testing pendiente](#deuda-de-testing-pendiente)) — a diferencia de spec 13, que sí probó cada comportamiento raro explícitamente, acá solo se probó el camino feliz. Un refactor futuro de `VerificadorDeOpciones` podría cambiar silenciosamente el comportamiento de `NullPointerException`/lista vacía/cardinalidad sin que ningún test lo detecte.
- **`content.api` creció a 12 interfaces + 7 Views nuevas en un solo commit**, todas siguiendo el mismo esqueleto mecánico (`Obtener...Query`/`ActualizarCritico...Command` por tipo). Es la superficie pública de un slice completo escrita de una sola vez sin iteración — mayor probabilidad de que algún detalle quede inconsistente entre los 6 tipos (p. ej. nombres de campo) sin que se note hasta que algo la consuma distinto.
- **El fallback muerto en `VerificarRespuestaController` (`PreguntaService` + rama `else`) sigue siendo un séptimo punto de registro manual no eliminado** — mismo riesgo que documentaba el borrador para un tipo de pregunta futuro, pero ahora además con código inalcanzable conviviendo con el dispatch real, lo que dificulta leer el controller y confirmar que efectivamente no se usa.
- **Este documento fue reescrito una vez para reflejar la realidad; nada garantiza que no vuelva a divergir.** Si se sigue construyendo sobre `answering`/`content.api` sin actualizar este spec u otros, el patrón de "el código avanza, el spec se queda atrás" se repite. Vale la pena decidir si specs como este se tratan como documentación viva (se actualizan con cada divergencia) o como bitácora histórica (se cierran y cualquier cambio real abre un spec nuevo, sin reescribir los viejos) — hoy conviven ambos criterios en el repo.
