# Spec 16 — Limpieza de código muerto: clases `Service` legacy y su andamiaje exclusivo

**Estado:** Implementado
**Dependencias:** Spec 15 (deja `PreguntaController`/`VerificarRespuestaController`/`TemarioController` sin ningún caller de producción hacia `Service.PreguntaService`/`TemarioService`/`ResponderService`).
**Fecha:** 2026-08-02
**Objetivo:** Eliminar `Service/PreguntaService`, `Service/TemarioService`, `Service/ResponderService` y todo el código que solo ellas mantenían alcanzable (7 repositorios de `Repository/PreguntaRepository/`, `FabricaDePreguntas`, `JsonVisualizador`, dos conversores DTO muertos y sus métodos), junto con los 15 tests de paridad que las usaban como baseline y dos limpiezas triviales sin relación (`Setup.java` no-op, 4 tests con cuerpo comentado).

---

## Alcance

### Incluido

**Clases muertas en producción (sin ningún caller fuera de sí mismas ni de tests), confirmado por grep exhaustivo en `src/main`:**

- `Service/PreguntaService.java`
- `Service/TemarioService.java`
- `Service/ResponderService.java`
- `Repository/PreguntaRepository/` completo (7 archivos): `PreguntaRepository.java`, `SeleccionUnicaRepository.java`, `OpcionMultipleRepository.java`, `DesplegableCompartidoRepositorio.java`, `DesplegableIndependienteRepository.java`, `PreguntaSimpleRepository.java`, `BasePreguntaRepositorio.java` — su único caller era `PreguntaService`.
- `model/AResponder/FabricaDePreguntas.java` — su único caller era `PreguntaService.createaQuestion`.
- `JsonVisualizador.java` — su único caller era `PreguntaService`.
- `dto/conversor/AResponseItemDTOConversor.java` — alcanzable solo vía `AResponder.toResponderItemListDTO()`, método sin ningún caller en `src/main` ni en tests.
- `dto/conversor/TemarioDTOConversor.java` — alcanzable solo vía `TemarioService` (muerta) y `Temario.toTemarioCuestionarioCardDTO()`, método sin ningún caller en `src/main` ni en tests.
- El método `AResponder.toResponderItemListDTO()` se borra de la clase `AResponder` (que sigue viva); `Temario.toTemarioCuestionarioCardDTO()` se borra de la clase `Temario` (que sigue viva) — ambos quedan sin caller una vez borrado su conversor asociado.
- **[Hallazgo durante la implementación]** `Temario.toTemarioCuestionarioWhitItemList()` también se borra. No estaba en el análisis original: se detectó al editar `Temario.java` para el paso anterior. Es código muerto real (cero callers en `src/main` ni `src/test`, confirmado por grep), pero además su cuerpo llamaba a `AResponder::toResponderItemListDTO` vía method reference — al borrar ese método (punto anterior), `toTemarioCuestionarioWhitItemList()` dejaba de compilar. Se detuvo la implementación y se presentó como ambigüedad al usuario, que aprobó borrarlo junto con sus 4 imports asociados (`TemarioDTOConversor`, `AResponderItemListDTO`, `TemarioBasicDTO`, `TemarioCuestionarioWhitItemListDTO`).

**Tests (se eliminan, no se reescriben):**

- Los 15 tests de paridad que instancian `PreguntaService`/`TemarioService`/`ResponderService` como baseline de comparación: `content/ResponderParidadTest.java`, `content/IssueItemsParidadTest.java`, `content/IssueQuestionIdsInverseParidadTest.java`, `content/IssueParidadTest.java`, `content/QuestionInverseParidadTest.java`, `content/EliminarPreguntaPorIdParidadTest.java`, `content/PreguntaSimpleParidadTest.java`, `content/VerdaderoOFalsoParidadTest.java`, `content/SeleccionUnicaParidadTest.java`, `content/OpcionMultipleParidadTest.java`, `content/DesplegableCompartidoParidadTest.java`, `content/DesplegableIndependienteParidadTest.java`, `answering/VerificarRespuestaPreguntaSimpleParidadTest.java`, `answering/VerificarRespuestaVerdaderoOFalsoParidadTest.java`, `answering/VerificarRespuestaTiposNoMigradosNoRegresionTest.java`.
- `content/FetchQuestionTiposNoMigradosNoRegresionTest.java` — no es paridad, pero su `@AfterEach` depende de `Repository.PreguntaRepository.PreguntaRepository` (paquete que se borra) para el cleanup.
- **[Hallazgo durante la implementación]** `content/ObtenerPreguntaSimpleContentTest.java` y `content/ObtenerVerdaderoOFalsoContentTest.java` — no detectados por el análisis original del spec. Tampoco son paridad (prueban directamente `ObtenerPreguntaUseCase`/`ObtenerPreguntaFullUseCase`/`ObtenerVerdaderoOFalsoUseCase`/`ObtenerVerdaderoOFalsoFullUseCase`, sin comparar contra las clases `Service`), pero cada uno tenía un campo `@Autowired` a un repositorio legacy borrado (`PreguntaSimpleRepository` / `PreguntaRepository`, respectivamente) usado únicamente en su `@AfterEach` para el cleanup vía `deleteById` — misma situación que `FetchQuestionTiposNoMigradosNoRegresionTest` arriba. Se detuvo la implementación y se presentó como ambigüedad al usuario, con dos opciones: recablear el cleanup a `EliminarPreguntaPorIdUseCase` (preservando los tests) o borrar ambos archivos (consistente con la decisión ya tomada para `FetchQuestionTiposNoMigradosNoRegresionTest`). El usuario eligió borrar ambos.

**Limpieza trivial sin relación con las clases `Service` (incluida a pedido explícito):**

- `config/setup/Setup.java` — bean `@Component implements ApplicationRunner` cuyo `run()` está enteramente comentado (no-op); `datos()`/`guardarCuestionario()` no tienen ningún caller en `src/main` ni en tests. Se borra el archivo completo.
- Los 4 tests con cuerpo íntegramente comentado: `src/test/java/com/lorenzomar3/AQ/Pregunta/DesplegableCompartidoTest.java`, `DesplegableIndependienteTest.java`, `OpcionMultipleTest.java`, `SeleccionUnicaTest.java`.

### Explícitamente NO incluido

- **`Repository/AResponderRepository.java` y `Repository/TemarioRepository.java` (legacy)** — a pesar del nombre, siguen vivas en producción: las usa el lado hexagonal (`content/infrastructure/persistence/adapter/TemarioJpaAdapter` y `AResponderTipoLookupJpaAdapter`, con campos nombrados explícitamente `...Viejo`) para dos queries SQL nativas recursivas y un `deleteById`/`findById`. Sacarlas requiere portar esas queries a un repositorio propio de `content/infrastructure` — es una migración en sí misma, no borrado de código muerto. Queda documentado como deuda para un spec futuro.
- **`model/AResponder/**` (el resto del árbol: `AResponder`, `Pregunta`, `Temario/Temario.java`, todo `TiposDePreguntas/*` incluyendo `Opcion`, `TeoriaDeLaPregunta`, `AsignadorDeTipoALasPreguntas`, `TipoDeTemario`/`TipoCuestionario`/`TipoTema`, etc.)** — sigue vivo: es el tipo de entidad detrás de `AResponderRepository`/`TemarioRepository` (arriba), lo usa `PreguntaController` para construir a mano la respuesta de `PUT /questions` (métodos `editarPreguntaSimple`, etc.), y sus clases de valor (`Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`, `TeoriaDeLaPregunta`) están incrustadas como tipo de campo en `PostPreguntaDTO`, `RespuestaDePreguntaDTO` y `CreateQuestionResponseDTO` — los DTOs que consumen todos los `UseCase`/`Handler` de `content/` y `answering/`. Sacarlo de raíz exige reemplazar esas clases de valor en los 3 DTOs y ajustar todo lo que construye esos DTOs a mano (mayormente tests). Queda fuera de este spec y documentado como deuda para un spec futuro.
- **`PreguntaController` no se modifica** — sus métodos `editarPreguntaSimple`/`editarVerdaderoOFalso`/etc. siguen construyendo objetos de `model.AResponder.TiposDePreguntas.*` a mano para la respuesta de `PUT /questions`, sin cambios (consecuencia directa del punto anterior).
- **`model/View.java`, `TipoAResponder`** — no se tocan, siguen en uso activo por ambos lados (legacy y hexagonal).
- **Pérdida de cobertura de tests aceptada explícitamente:** al borrar los 15 tests de paridad sin reemplazo, varios `UseCase`/`Handler` de `content/` y `answering/` (todos los `Crear*`/`Editar*` de los 6 tipos de pregunta, `EliminarPreguntaPorIdUseCase`, `ObtenerIdsAleatoriosDePreguntasUseCase`, `ObtenerIdsCriticosUseCase`, `ObtenerItemsDeIssueUseCase`, `CrearCuestionarioUseCase`, `CrearIssueUseCase`/`EditarIssueUseCase`/`EliminarIssueUseCase`, `ObtenerIdsDePreguntasUseCase`, `CrearIssueInversoUseCase`, `CrearPreguntaInversaHandler`, y los 6 `VerificarRespuesta*Handler`) quedan **sin ningún test automatizado** — hoy solo estaban cubiertos ahí. No se agrega cobertura de reemplazo en este spec.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

No aplica — este spec no introduce ni modifica ninguna estructura de datos. Es puramente eliminación de código muerto (clases, métodos y tests); ningún DTO, entidad ni tabla cambia de forma.

---

## Plan de implementación

1. **Borrar los 15 tests de paridad + `FetchQuestionTiposNoMigradosNoRegresionTest`** (16 archivos, listados en Alcance). Se borran primero para que el árbol de tests deje de referenciar las clases `Service`/repos legacy antes de tocar producción, evitando un estado intermedio que no compile.
2. **Borrar los 4 tests con cuerpo comentado** de `src/test/java/com/lorenzomar3/AQ/Pregunta/`.
3. **Borrar `Service/PreguntaService.java`, `Service/TemarioService.java`, `Service/ResponderService.java`.**
4. **Borrar `Repository/PreguntaRepository/` completo** (7 archivos).
5. **Borrar `model/AResponder/FabricaDePreguntas.java`.**
6. **Borrar `JsonVisualizador.java`.**
7. **Borrar `dto/conversor/AResponseItemDTOConversor.java` y `dto/conversor/TemarioDTOConversor.java`**, y los métodos `AResponder.toResponderItemListDTO()` / `Temario.toTemarioCuestionarioCardDTO()` que quedan sin caller tras el paso anterior. **[Ampliado durante la implementación]** también se borró `Temario.toTemarioCuestionarioWhitItemList()` (código muerto adicional detectado en este paso, ver Alcance → Incluido).
8. **Borrar `config/setup/Setup.java`.**
9. **[Paso agregado durante la implementación]** Borrar `content/ObtenerPreguntaSimpleContentTest.java` y `content/ObtenerVerdaderoOFalsoContentTest.java` (código de test no detectado en el análisis original, ver Alcance → Tests).
10. **Verificación.** Correr `./mvnw test` (lo corre el usuario) — debe compilar y pasar completo, sin ningún residuo de `import` o referencia a las clases borradas en el resto del código (controllers, otros tests, `Repository/AResponderRepository`/`TemarioRepository`, `content/`, `answering/`). Confirmar con grep que no quedan referencias a `Service.PreguntaService`, `Service.TemarioService`, `Service.ResponderService`, `Repository.PreguntaRepository.*`, `FabricaDePreguntas`, `JsonVisualizador`, `AResponseItemDTOConversor`, `TemarioDTOConversor` en `src/main` ni `src/test`.

---

## Criterios de aceptación

- [x] `Service/PreguntaService.java`, `Service/TemarioService.java`, `Service/ResponderService.java` no existen en el repositorio.
- [x] `Repository/PreguntaRepository/` no existe (los 7 archivos borrados).
- [x] `model/AResponder/FabricaDePreguntas.java` y `JsonVisualizador.java` no existen.
- [x] `dto/conversor/AResponseItemDTOConversor.java` y `dto/conversor/TemarioDTOConversor.java` no existen.
- [x] `AResponder.java` ya no tiene el método `toResponderItemListDTO()`; `Temario.java` ya no tiene los métodos `toTemarioCuestionarioCardDTO()` ni `toTemarioCuestionarioWhitItemList()` (este último, hallazgo agregado durante la implementación).
- [x] `config/setup/Setup.java` no existe.
- [x] Los 15 tests de paridad listados en Alcance no existen.
- [x] `content/FetchQuestionTiposNoMigradosNoRegresionTest.java` no existe.
- [x] `content/ObtenerPreguntaSimpleContentTest.java` y `content/ObtenerVerdaderoOFalsoContentTest.java` no existen (hallazgo agregado durante la implementación).
- [x] Los 4 tests con cuerpo comentado de `src/test/.../Pregunta/` no existen.
- [x] Ningún archivo bajo `src/main` o `src/test` importa o referencia ninguna de las clases borradas (verificado por grep).
- [x] `Repository/AResponderRepository.java`, `Repository/TemarioRepository.java` y el resto de `model/AResponder/**` siguen existiendo sin cambios — no fueron tocados (fuera de alcance).
- [x] `PreguntaController`, `VerificarRespuestaController`, `TemarioController`, `ResponderController` no fueron modificados.
- [x] `./mvnw test` corre completo y pasa — confirmado por el usuario.
- [x] `./mvnw package` compila sin errores — confirmado por el usuario.

---

## Decisiones tomadas

- **Se eliminan los 15 tests de paridad directamente, sin reescribirlos a hexagonal puro.** La alternativa (quitar el `@Autowired` a la clase `Service` y conservar solo las aserciones contra el `UseCase`/`Handler` nuevo) habría preservado cobertura para ~20 casos de uso que hoy no tienen ningún otro test. Se descartó explícitamente: el usuario priorizó avanzar con la limpieza sobre invertir el trabajo de reescritura, aceptando la pérdida de cobertura como consecuencia conocida (documentada en Alcance → "Explícitamente NO incluido").
- **`Repository/AResponderRepository`/`TemarioRepository` (legacy) y el resto de `model/AResponder/**` quedan fuera de este spec**, pese a que la primera pasada de scoping los había incluido. La razón del cambio: no son código muerto en sentido estricto — el lado hexagonal (`TemarioJpaAdapter`, `AResponderTipoLookupJpaAdapter`) depende de ellos para dos queries SQL nativas recursivas, y `PostPreguntaDTO`/`RespuestaDePreguntaDTO`/`CreateQuestionResponseDTO` (consumidos por todo `content/` y `answering/`) usan sus clases de valor (`Opcion`, `OpcionDeDesplegableCompartido`, `SeleccionUnicaParaDesplegableIndependiente`, `TeoriaDeLaPregunta`) como tipo de campo. Sacarlos de raíz es una migración de los contratos de entrada/salida del slice, no un borrado de código muerto — se prefirió mantener este spec acotado a lo que es indiscutiblemente inalcanzable, y dejar esa migración para un spec futuro dedicado (con su propio diseño de reemplazo para esos value types).
- **Se incluye `config/setup/Setup.java` y los 4 tests con cuerpo comentado**, pese a no tener relación directa con las clases `Service`. Ambos son limpieza trivial y de riesgo cero (cero callers verificados), y el objetivo explícito del usuario para esta sesión es "seguir eliminando código viejo o legacy" en términos amplios — no había motivo para abrir un spec aparte para dos borrados sin ningún costo de diseño.
- **Se borran los métodos `AResponder.toResponderItemListDTO()` y `Temario.toTemarioCuestionarioCardDTO()`** aunque las clases que los contienen (`AResponder`, `Temario`) siguen vivas. Son código muerto real (cero callers) que solo se detectó al rastrear por qué `AResponseItemDTOConversor`/`TemarioDTOConversor` parecían "alcanzables" — se documenta la excepción para que quede claro que no se está tocando el resto de esas dos clases.
- **El orden del plan borra los tests antes que la producción** (pasos 1-2 antes que 3-8) para que en ningún punto intermedio el árbol quede sin compilar — si se borrara `Service/PreguntaService` primero, los 15 tests de paridad y el de fetch dejarían de compilar antes de haber sido removidos.
- **[Decisión tomada durante la implementación] Se borra `Temario.toTemarioCuestionarioWhitItemList()`** aunque no estaba en el análisis original. Al borrar `AResponder.toResponderItemListDTO()` (paso 7), este método —también sin ningún caller— dejaba de compilar por depender de él vía method reference. Se presentó como ambigüedad y el usuario aprobó tratarlo igual que los otros dos métodos huérfanos del mismo paso: borrarlo junto con sus imports.
- **[Decisión tomada durante la implementación] Se borran `content/ObtenerPreguntaSimpleContentTest.java` y `content/ObtenerVerdaderoOFalsoContentTest.java`** en vez de recablear su `@AfterEach` al `UseCase` de borrado hexagonal. Ambos son tests de regresión reales (no paridad) para `ObtenerPreguntaUseCase`/`ObtenerPreguntaFullUseCase`/`ObtenerVerdaderoOFalsoUseCase`/`ObtenerVerdaderoOFalsoFullUseCase`, y su única dependencia de las clases borradas era un repo legacy autowireado solo para el cleanup. Se ofrecieron ambas alternativas (recablear vs. borrar) y el usuario eligió borrar, consistente con la decisión ya tomada para `FetchQuestionTiposNoMigradosNoRegresionTest` (ver Riesgos).

---

## Riesgos identificados

- **Pérdida real de red de contención para ~20 `UseCase`/`Handler` de `content/`/`answering/`.** Con los 15 tests de paridad borrados, un cambio futuro en cualquiera de los `Crear*`/`Editar*`/`EliminarPreguntaPorId`/`ObtenerIds*`/`VerificarRespuesta*` puede introducir una regresión de comportamiento sin que ningún test automatizado la detecte — antes, la comparación explícita contra `PreguntaService`/`TemarioService` la habría atrapado. *Mitigación:* ninguna en este spec (aceptado explícitamente); queda como trabajo pendiente escribir tests unitarios/de integración nuevos para esos casos de uso, sin la muleta de comparación contra el camino viejo.
- **`FetchQuestionTiposNoMigradosNoRegresionTest` se pierde por completo**, y no era un test de paridad comparativa sino un test de regresión real vía `MockMvc` con JSON esperado hardcodeado para `fetch`/`fetch-full` de los 4 tipos "no migrados". Se decidió borrarlo junto con el paquete `Repository/PreguntaRepository/` en vez de recablear su única línea de cleanup (`preguntaRepositoryViejo.deleteById`) a `EliminarPreguntaPorIdUseCase`, que habría sido un cambio de una línea. *Mitigación:* ninguna — decisión explícita del usuario de eliminar en vez de recablear.
- **[Riesgo detectado durante la implementación] `ObtenerPreguntaSimpleContentTest` y `ObtenerVerdaderoOFalsoContentTest` se pierden por completo**, no detectados por el análisis original del spec (su única referencia a las clases borradas era un `@Autowired` a un repo legacy usado solo en `@AfterEach`). Eran la única cobertura automatizada de `ObtenerPreguntaUseCase`, `ObtenerPreguntaFullUseCase`, `ObtenerVerdaderoOFalsoUseCase` y `ObtenerVerdaderoOFalsoFullUseCase`. Al igual que con `FetchQuestionTiposNoMigradosNoRegresionTest`, se ofreció recablear el cleanup (cambio de una línea) como alternativa a borrar, y el usuario eligió borrar. *Mitigación:* ninguna en este spec (aceptado explícitamente) — queda como trabajo pendiente escribir tests nuevos para esos 4 `UseCase`, ahora sin ningún test automatizado.
- **La superficie "legacy" del repo queda partida en dos estados distintos tras este spec**, lo cual puede confundir a quien lea el código sin el contexto de este documento: `Service/PreguntaService`/`TemarioService`/`ResponderService` (código muerto, ahora inexistente) vs. `Repository/AResponderRepository`/`TemarioRepository` + `model/AResponder/**` (con nombres y ubicación que sugieren lo mismo, pero vivos y con callers reales en el lado hexagonal, incluyendo campos literalmente sufijados `...Viejo`). *Mitigación:* la sección "Explícitamente NO incluido" de este spec documenta por qué esas clases sobreviven, para que la próxima sesión no asuma que son borrado pendiente trivial.
- **El spec futuro que reemplace `model/AResponder/**` es de alcance considerable** (nuevos value types para `PostPreguntaDTO`/`RespuestaDePreguntaDTO`/`CreateQuestionResponseDTO`, reescritura de todo lo que construye esos DTOs a mano) y no tiene fecha ni número asignado todavía. *Mitigación:* ninguna necesaria ahora — es trabajo futuro fuera del alcance de este spec, solo se deja registrado como deuda conocida.
