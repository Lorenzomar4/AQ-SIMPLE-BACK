# Spec 18 — Frente B: eliminar la dependencia de producción sobre `model/AResponder/**` en los DTOs de pregunta y `updateQuestion`, y borrar el árbol legacy completo

**Estado:** Implementado
**Dependencias:** Spec 17 (deja `model/AResponder/**` sin callers desde el lado de persistencia — `TemarioJpaAdapter`/`AResponderTipoLookupJpaAdapter` ya usan `AResponderEntity` hexagonal; los únicos callers de producción restantes son `PostPreguntaDTO`, `RespuestaDePreguntaDTO` y `PreguntaController.updateQuestion`).
**Fecha:** 2026-08-02
**Objetivo:** Reemplazar las 4 clases de valor legacy (`Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`, `TeoriaDeLaPregunta`) en `PostPreguntaDTO`/`RespuestaDePreguntaDTO` por sus equivalentes hexagonales de `content.domain`, reescribir `PreguntaController.updateQuestion` para que devuelva el objeto de dominio hexagonal directamente (en vez de construir un `Pregunta` legacy a mano), simplificar los 12 `Crear*/Editar*Service` eliminando sus conversiones manuales ahora redundantes, corregir el único acoplamiento roto que esto introduce en `VerificarRespuestaController`, y borrar por completo el árbol `model/AResponder/**` (23 archivos) que queda inalcanzable.

---

## Alcance

### Incluido

- **`content/domain/TeoriaDeLaPregunta.java` (nuevo)** — value type plano (sin `@Entity`, sin relaciones JPA), campos `id: Long`, `respuesta: String`, `imagen: String` — misma forma que la clase legacy, pero como POJO con `@Getter @Setter @NoArgsConstructor` (mismo estilo que `content.domain.Opcion` y compañía). Paridad estructural únicamente: ningún `Crear*/Editar*Service` lo lee ni escribe, igual que hoy.
- **`dto/newDto/PostPreguntaDTO.java` (modificado)** — los 4 campos de lista pasan de tipos `model.AResponder.**` a sus equivalentes `content.domain.*`: `List<TeoriaDeLaPregunta>`, `List<Opcion>`, `List<OpcionDeDesplegableCompartido>`, `List<SeleccionUnicaParaDesplegableIndependiente>`.
- **`dto/newDto/RespuestaDePreguntaDTO.java` (modificado)** — los 3 campos de lista (`Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`) pasan a `content.domain.*`.
- **`Controller/PreguntaController.java` (modificado):**
  - `mapDeEdicion` pasa de `Map<TipoAResponder, Function<PostPreguntaDTO, Pregunta>>` a `Map<TipoAResponder, Function<PostPreguntaDTO, Object>>`, apuntando directo a los 6 `Editar*UseCase::editar` (mismo patrón ya usado en `mapDeCreacion`).
  - Se eliminan los 6 métodos privados `editarPreguntaSimple`/`editarVerdaderoOFalso`/`editarSeleccionUnica`/`editarOpcionMultiple`/`editarDesplegableCompartido`/`editarDesplegableIndependiente` (132 líneas) — ya no hace falta reconstruir ningún objeto legacy a mano.
  - `updateQuestion` cambia su tipo de retorno a `ResponseEntity<Object>`, se quita `@JsonView(View.JustToAnswer.class)` — devuelve el objeto de dominio hexagonal tal cual, mismo patrón que `getQuestion`/`getQuestionFull`.
  - Se eliminan los 9 imports muertos de `model.AResponder.**`.
- **Los 12 `Crear*Service`/`Editar*Service`** de `SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente` (los que hoy tienen `convertirOpciones`/`convertirListaDeOpciones`): se elimina el método privado de conversión y se usa directamente `postPreguntaDTO.listaDeXxx()`, ya tipado en `content.domain`. Los 4 de `PreguntaSimple`/`VerdaderoOFalso` no tienen conversión de listas y no cambian de lógica, solo pierden imports muertos si los tuvieran.
- **`VerificarRespuestaController.verificarDesplegableIndependiente` (modificado, 1 línea):** `subPregunta.getListaDeOpcionesDisponible()` → `subPregunta.getListaDeOpciones()` — único breaking rename detectado, porque `content.domain.SeleccionUnicaParaDesplegableIndependiente` nombra el campo `listaDeOpciones` en vez de `listaDeOpcionesDisponible`.
- **Borrado completo de `model/AResponder/**`** (23 archivos): `AResponder.java`, `Pregunta.java`, `IPregunta.java`, `TeoriaDeLaPregunta.java`, `AsignadorDeTipoALasPreguntas.java`, `TiposDePreguntas/{ITemaPregunta,IPreguntaVariasOpciones,Opcion,PreguntaSimple,VerdaderoOFalso,SeleccionUnica,OpcionMultiple,RespuestaDeDesplegableIndependiente}.java`, `TiposDePreguntas/Verificador/{IRespuestaOpcion,Verificador}.java`, `TiposDePreguntas/DesplegableCompartido/{DesplegableCompartido,OpcionDeDesplegableCompartido}.java`, `TiposDePreguntas/DesplegabeIndependiente/{DesplegableIndependiente,SeleccionUnicaParaDesplegableIndependiente}.java`, `Temario/{Temario,TipoTema,TipoCuestionario,TipoDeTemario}.java` — confirmado por grep sin ningún caller fuera del propio árbol una vez aplicados los cambios anteriores.
- **Tests de integración nuevos:**
  - `PUT /questions` para los 6 tipos de pregunta (verificar que el JSON devuelto refleja los cambios y que el objeto de dominio se persiste correctamente).
  - `POST /questions/verify` al menos para los 4 tipos con listas de opciones (`SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`), cubriendo en particular el caso `DesplegableIndependiente` que ejercita el getter corregido.

### Explícitamente NO incluido

- **Dar funcionalidad real a `TeoriaDeLaPregunta`** — sigue siendo un campo sin consumidor en `Crear*/Editar*Service`; solo se migra su tipo.
- **`CreateQuestionResponseDTO`** — no depende de ninguna clase legacy, no cambia.
- **`createQuestion` (POST /questions)** — su lógica de dispatch no cambia; se beneficia indirectamente de la simplificación de los `Crear*Service`, pero el controller no se toca en ese método.
- Cualquier cambio de esquema de base de datos (las tablas legacy como `teoria_de_la_pregunta` siguen existiendo, simplemente sin ninguna entidad JPA que las mapee).
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

```java
// content/domain/TeoriaDeLaPregunta.java (nuevo)
package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TeoriaDeLaPregunta {

    private Long id;
    private String respuesta;
    private String imagen;
}
```

```java
// dto/newDto/PostPreguntaDTO.java (modificado)
package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.TipoAResponder;

import java.util.List;

public record PostPreguntaDTO(
    Long id, String titulo, String descripcion, TipoAResponder tipo,
    Long idTemarioPerteneciente, Boolean respuestaVerdadera, String respuestaEstablecida,
    List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta,
    List<Opcion> listaDeOpcionesConSuRespuestaReal,
    List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido,
    List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente) {}
```

```java
// dto/newDto/RespuestaDePreguntaDTO.java (modificado)
package com.lorenzomar3.AQ.dto.newDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.TipoAResponder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaDePreguntaDTO(
    Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana,
    List<Opcion> listaDeOpciones,
    List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos,
    List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente) {}
```

```java
// Controller/PreguntaController.java — dentro de @PostConstruct init(), reemplaza el bloque actual de mapDeEdicion
mapDeEdicion.put(TipoAResponder.PREGUNTA_SIMPLE, editarPreguntaUseCase::editar);
mapDeEdicion.put(TipoAResponder.VERDADERO_FALSO, editarVerdaderoOFalsoUseCase::editar);
mapDeEdicion.put(TipoAResponder.SELECCION_UNICA, editarSeleccionUnicaUseCase::editar);
mapDeEdicion.put(TipoAResponder.OPCION_MULTIPLE, editarOpcionMultipleUseCase::editar);
mapDeEdicion.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, editarDesplegableCompartidoUseCase::editar);
mapDeEdicion.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, editarDesplegableIndependienteUseCase::editar);

// campo, ya no Function<PostPreguntaDTO, Pregunta>
private final Map<TipoAResponder, Function<PostPreguntaDTO, Object>> mapDeEdicion = new HashMap<>();

// endpoint, ya sin @JsonView ni import de Pregunta legacy
@PutMapping("/questions")
public ResponseEntity<Object> updateQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
    logger.info("[PUT /questions] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipo());

    Function<PostPreguntaDTO, Object> editar = mapDeEdicion.get(getQuestionDTO.tipo());
    if (editar == null) {
        throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
    }

    return new ResponseEntity<>(editar.apply(getQuestionDTO), HttpStatus.OK);
}
```

```java
// content/application/service/EditarSeleccionUnicaService.java — ejemplo del patrón de limpieza
// (mismo cambio en Editar/Crear de OpcionMultiple, DesplegableCompartido, DesplegableIndependiente)
@Override
@Transactional
public SeleccionUnica editar(PostPreguntaDTO postPreguntaDTO) {
    SeleccionUnica seleccionUnica = seleccionUnicaRepositoryPort.findById(postPreguntaDTO.id())
            .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

    seleccionUnica.setTitulo(postPreguntaDTO.titulo());
    seleccionUnica.setDescripcion(postPreguntaDTO.descripcion());
    seleccionUnica.setListaDeOpciones(postPreguntaDTO.listaDeOpcionesConSuRespuestaReal());

    return seleccionUnicaRepositoryPort.save(seleccionUnica);
}
// se elimina el método privado convertirOpciones(...) completo
```

```java
// answering/infrastructure/controller/VerificarRespuestaController.java — línea 126, único rename necesario
.map(subPregunta -> new SubPreguntaRespuestaDTO(subPregunta.getId(), subPregunta.getListaDeOpciones().stream()
```

---

## Plan de implementación

1. **Crear `content/domain/TeoriaDeLaPregunta.java`** (value type plano, ver Modelo de datos). El sistema sigue compilando y funcionando igual (nadie lo referencia todavía).
2. **Modificar `PostPreguntaDTO.java` y `RespuestaDePreguntaDTO.java`** para tipar sus campos de lista con `content.domain.*` en vez de las 4 clases legacy. Este cambio por sí solo rompe la compilación de `PreguntaController`, los 12 `Crear*/Editar*Service` afectados y `VerificarRespuestaController` — no hay forma de aislarlo en un paso compilable de forma independiente, así que los pasos 2-5 se aplican como una sola unidad de cambio.
3. **Reescribir `PreguntaController.updateQuestion`**: nuevo `mapDeEdicion` apuntando directo a los 6 `Editar*UseCase::editar`, eliminación de los 6 métodos privados `editarXxx`, cambio de tipo de retorno a `ResponseEntity<Object>`, remoción de `@JsonView(View.JustToAnswer.class)` y de los 9 imports muertos de `model.AResponder.**` (ver Modelo de datos).
4. **Simplificar los 12 `Crear*Service`/`Editar*Service`** de `SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`: eliminar el método privado `convertirOpciones`/`convertirListaDeOpciones` de cada uno y usar directamente el campo ya tipado del DTO.
5. **Corregir `VerificarRespuestaController.verificarDesplegableIndependiente`**: `subPregunta.getListaDeOpcionesDisponible()` → `subPregunta.getListaDeOpciones()`.
6. **Compilar y confirmar que el proyecto vuelve a compilar limpio** tras los pasos 2-5 (interdependientes entre sí).
7. **Grep de verificación**: confirmar que ningún archivo de `src/main` ni `src/test` referencia `com.lorenzomar3.AQ.model.AResponder.*` fuera del propio árbol.
8. **Borrar `model/AResponder/**` completo** (23 archivos, listados en Alcance).
9. **Escribir los tests de integración nuevos**: `PUT /questions` para los 6 tipos de pregunta, y `POST /questions/verify` para los 4 tipos con listas de opciones (`SeleccionUnica`, `OpcionMultiple`, `DesplegableCompartido`, `DesplegableIndependiente`), este último cubriendo explícitamente el caso `DesplegableIndependiente` que ejercita el getter corregido en el paso 5.
10. **Verificación.** Correr `./mvnw test` y `./mvnw package` (lo corre el usuario) — deben compilar y pasar completo, incluyendo los tests nuevos. Verificación manual contra `AQ-SIMPLE-FRONT`: editar una pregunta de cada uno de los 6 tipos (`PUT /questions`) y confirmar que el formulario de edición sigue funcionando con el nuevo shape de respuesta; responder preguntas de los 4 tipos con opciones (`POST /questions/verify`) para confirmar que el rename del paso 5 no rompió nada.

---

## Criterios de aceptación

- [x] `content/domain/TeoriaDeLaPregunta.java` existe, es un POJO plano (`id`, `respuesta`, `imagen`), sin `@Entity` ni anotaciones JPA.
- [x] `PostPreguntaDTO.java` tipa sus 4 campos de lista con `content.domain.TeoriaDeLaPregunta`/`Opcion`/`OpcionDeDesplegableCompartido`/`SeleccionUnicaParaDesplegableIndependiente`, sin ningún import de `model.AResponder.**`.
- [x] `RespuestaDePreguntaDTO.java` tipa sus 3 campos de lista con los equivalentes `content.domain.*`, sin ningún import de `model.AResponder.**`.
- [x] `PreguntaController.updateQuestion` devuelve `ResponseEntity<Object>`, sin `@JsonView(View.JustToAnswer.class)`, y `mapDeEdicion` apunta directo a los 6 `Editar*UseCase::editar`.
- [x] Los 6 métodos privados `editarPreguntaSimple`/`editarVerdaderoOFalso`/`editarSeleccionUnica`/`editarOpcionMultiple`/`editarDesplegableCompartido`/`editarDesplegableIndependiente` ya no existen en `PreguntaController`.
- [x] `PreguntaController` no tiene ningún import de `model.AResponder.**`.
- [x] Los `Crear*Service`/`Editar*Service` de `SeleccionUnica`/`OpcionMultiple`/`DesplegableCompartido`/`DesplegableIndependiente` ya no tienen el método privado `convertirOpciones`/`convertirListaDeOpciones`. **Nota:** son 8 archivos (Crear+Editar de cada uno de los 4 tipos), no 12 como decía este criterio originalmente — desajuste de conteo detectado durante la implementación, confirmado por grep sin ningún `convertirOpciones`/`convertirListaDeOpciones` restante en el árbol.
- [x] `VerificarRespuestaController.verificarDesplegableIndependiente` usa `getListaDeOpciones()`, no `getListaDeOpcionesDisponible()`.
- [x] `model/AResponder/**` no existe en el repositorio (los 23 archivos listados en Alcance, incluyendo `Temario`/`TipoTema`/`TipoCuestionario`/`TipoDeTemario` legacy).
- [x] Ningún archivo bajo `src/main` o `src/test` referencia `com.lorenzomar3.AQ.model.AResponder.*` (verificado por grep).
- [x] `CreateQuestionResponseDTO.java` y `createQuestion` (POST /questions) no fueron modificados (fuera de alcance).
- [x] Existen tests de integración nuevos cubriendo `PUT /questions` para los 6 tipos de pregunta y `POST /questions/verify` para los 4 tipos con listas de opciones.
- [x] `./mvnw test` corre completo y pasa — confirmado por el usuario.
- [ ] `./mvnw package` compila sin errores — **no verificado, el usuario decidió avanzar sin correrlo.**
- [ ] Verificación manual contra `AQ-SIMPLE-FRONT` de `PUT /questions` (los 6 tipos) y `POST /questions/verify` (los 4 tipos con opciones) — **no verificado, el usuario decidió avanzar sin correrlo.**

---

## Decisiones tomadas

- **El Frente B se cierra en un solo spec, sin dividirlo en pasos** (a diferencia del Frente A, que spec 17 dividió explícitamente en "portar queries" + "borrado en cascada"). El usuario priorizó cerrar de punta a punta la dependencia sobre `model/AResponder/**`, incluyendo el borrado físico del árbol, en vez de dejarlo para un "Frente C" futuro.
- **Los 3 value types de `content.domain` ya existentes (`Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`) se reusan directo como tipo de campo del DTO HTTP**, en vez de crear DTOs de transporte separados del dominio. Se evaluó la alternativa (3 clases nuevas en `dto/newDto` desacopladas del dominio) pero se descartó: hoy esos mismos value types ya viajan sin cambios entre el borde HTTP y el dominio en los `Editar*Service` (solo con una conversión manual redundante en el medio, que este spec elimina), así que envolverlos en un DTO propio solo agregaría código sin ganar desacople real en la práctica actual del proyecto.
- **`TeoriaDeLaPregunta` se migra solo estructuralmente** (nuevo value type `content.domain.TeoriaDeLaPregunta`, sin ningún `Crear*/Editar*Service` que lo lea o escriba) en vez de eliminarse del contrato o dársele funcionalidad real. Se mantiene el mismo comportamiento "campo muerto" que tiene hoy — decisión explícita del usuario, no una limpieza de alcance ampliado.
- **La cadena `IPregunta`/`IPreguntaVariasOpciones`/`laRespuestaEsCorrecta`/`verificarSiLaRespuestaEsCorrectaYAsignarCriticos` desaparece como consecuencia directa del borrado íntegro de `model/AResponder/**`, no como una tarea de limpieza de código muerto tratada aparte.** Durante la Fase 2 se había preguntado inicialmente si tratar esa cadena (confirmada sin callers desde que `PreguntaService.verifyResponse` se borró en spec 16) como borrado explícito o dejarla fuera de alcance, y la respuesta fue dejarla fuera; la decisión posterior de borrar el árbol completo en este mismo spec la vuelve alcanzable igual, sin que haga falta documentarla ni verificarla como una preocupación separada.
- **`updateQuestion` devuelve el objeto de dominio hexagonal directo (`ResponseEntity<Object>`, sin `@JsonView`)**, igual que `getQuestion`/`getQuestionFull`, en vez de preservar el shape JSON exacto que hoy produce `@JsonView(JustToAnswer.class)` sobre `Pregunta` legacy (que excluye campos como `respuestaEstablecida` e incluye otros sin anotar). Se prefirió consistencia con el resto del controller y menos código nuevo, aceptando que el JSON de respuesta expone más campos que antes — el riesgo de esto se documenta en Riesgos.
- **Se simplifican también los 12 `Crear*Service`/`Editar*Service`** (no solo los 6 `Editar*` que motivaron el Frente B), eliminando sus métodos `convertirOpciones`/`convertirListaDeOpciones` ahora redundantes. Se incluye porque dejarlos como código muerto recién introducido por este mismo spec no tendría sentido — mismo criterio que llevó a spec 16 a incluir limpiezas triviales sin relación directa con su objetivo principal.
- **Se borra `Temario`/`TipoTema`/`TipoCuestionario`/`TipoDeTemario` legacy también**, aunque el objetivo original del Frente B (anotado en spec 17) hablaba solo de la rama `Pregunta`. Se incluye porque, confirmado por grep, no tiene ningún caller fuera de su propio paquete — dejarlo vivo solo generaría una segunda pieza de "legacy sin uso" que documentar, cuando ya se está borrando el resto del árbol en el mismo spec.
- **Se agregan tests nuevos para `PUT /questions` y `POST /questions/verify`**, revirtiendo el criterio de spec 16 de aceptar pérdida de cobertura sin reemplazo. Se justifica porque, a diferencia de los casos de spec 16 (donde se borraba código y se aceptaba perder su red de contención), acá este mismo spec **reescribe por completo** `updateQuestion` y **modifica el comportamiento** de `VerificarRespuestaController` — ambos sin ningún test hoy — por lo que no agregar cobertura dejaría el cambio más riesgoso sin verificar más que manualmente.

### Desviaciones detectadas durante la implementación

- **El orden del plan (pasos 2-5 → 6 compilar → 7 grep → 8 borrar) no era ejecutable tal cual estaba escrito.** Al compilar tras los pasos 2-5 apareció un acoplamiento inverso no detectado en el diseño: el propio árbol `model/AResponder/**` consume `RespuestaDePreguntaDTO` en varios puntos (`Pregunta.verificarSiLaRespuestaEsCorrectaYAsignarCriticos`, `IPreguntaVariasOpciones` y sus 4 implementaciones), no solo el rename ya anticipado en `VerificarRespuestaController`. Como ese código se borra en el mismo sped de todas formas, se resolvió fusionando el paso 8 (borrado del árbol legacy) con la unidad de cambio de los pasos 2-5, y recién ahí se compiló. El estado final es idéntico al descripto en el spec; solo cambió el orden interno de ejecución.
- **`model/View.java` se borró también**, aunque no estaba en la lista de 23 archivos del Alcance. Quedó sin ningún caller como efecto directo del paso 3 (se quitó el único `@JsonView(View.JustToAnswer.class)` que lo usaba en todo el código, confirmado por grep). Se aplicó el mismo criterio ya usado en este spec para los `Crear*/Editar*Service`: no tiene sentido dejar código muerto recién introducido por el propio spec.
- **`./mvnw package` y la verificación manual contra `AQ-SIMPLE-FRONT` quedaron sin correr** — el usuario decidió avanzar sin ellos tras confirmar que `./mvnw test` pasaba completo. Quedan como deuda de verificación pendiente, no como criterios cerrados.

---

## Riesgos identificados

- **El nuevo shape JSON de `updateQuestion` (objeto de dominio plano, sin `@JsonView`) expone más campos que el `Pregunta` legacy filtrado** (p.ej. `respuestaEstablecida` en `PreguntaSimple`, hoy excluido por ser `Full`-only). *Mitigación:* es el mismo shape que ya usan `getQuestion`/`getQuestionFull` desde hace varios specs — el frontend ya convive con esa forma en la ruta de lectura; se verifica manualmente contra `AQ-SIMPLE-FRONT` el flujo de edición completo de los 6 tipos antes de dar el spec por cerrado.
- **Cambiar `PostPreguntaDTO`/`RespuestaDePreguntaDTO` rompe simultáneamente la compilación de `PreguntaController`, los 12 `Crear*/Editar*Service` y `VerificarRespuestaController`**, sin punto intermedio compilable entre los pasos 2-5 del plan — a diferencia del patrón estrictamente incremental que pudieron seguir specs anteriores. *Mitigación:* el mapeo campo a campo entre las clases legacy y `content.domain` ya se verificó exhaustivamente durante el diseño de este spec (mismos nombres salvo el rename ya identificado en `VerificarRespuestaController`); el paso 6 exige compilar el conjunto antes de avanzar al borrado.
- **El rename `getListaDeOpcionesDisponible()` → `getListaDeOpciones()` en `VerificarRespuestaController` es el único punto de acoplamiento roto detectado por lectura manual de código**, no por haber corrido ya el compilador con el cambio real aplicado. Si existiera algún otro caller no detectado que dependa del nombre exacto de un getter legacy sobre estos value types, quedaría roto hasta compilar. *Mitigación:* paso 6 (compilar) + paso 7 (grep de verificación) antes de borrar el árbol legacy; los tests nuevos del paso 9 ejercitan explícitamente ese getter vía `DesplegableIndependiente`.
- **Al borrar el árbol legacy, las tablas de base de datos asociadas (p.ej. `teoria_de_la_pregunta`) dejan de tener cualquier entidad JPA que las referencie.** No se eliminan tablas ni columnas (`ddl-auto=update` no dropea), pero Hibernate deja de gestionar ese esquema silenciosamente. *Mitigación:* ninguna necesaria — comportamiento esperado, ya documentado como fuera de alcance en Alcance → "Explícitamente NO incluido".
- **La pérdida de cobertura de ~20 `UseCase`/`Handler` heredada de spec 16 no se cierra en este spec** — solo se agrega cobertura nueva para los dos flujos que este spec toca directamente (`PUT /questions`, `POST /questions/verify`). *Mitigación:* ninguna nueva — deuda conocida, ampliar cobertura al resto queda fuera de alcance.
