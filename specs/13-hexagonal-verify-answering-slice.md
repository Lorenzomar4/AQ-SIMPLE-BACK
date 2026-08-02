# Spec 13 — Migración hexagonal: POST /questions/verify (inaugura el slice answering/)

**Estado:** Implementado
**Dependencias:** Spec 01 (`PreguntaSimpleEntity`/`PreguntaSimpleJpaRepository`) y Spec 02 (`VerdaderoOFalsoEntity`/`VerdaderoOFalsoJpaRepository`) — se reutilizan directo desde la infraestructura de `answering`, sin pasar por los ports/domain/services de `content`.
**Fecha:** 2026-08-01
**Objetivo:** Migrar `POST /questions/verify` a arquitectura hexagonal para `PREGUNTA_SIMPLE` y `VERDADERO_FALSO`, inaugurando el slice `answering/` definido en `ARQUITECTURA.md` (dominio y puertos propios, sin importar `application`/`domain` de `content`, solo reutilizando las `Entity`/`JpaRepository` JPA de specs 01-02 desde infraestructura), dejando los otros 4 tipos de pregunta en el camino viejo (`PreguntaService.verifyResponse`) sin cambios.

---

## Alcance

### Incluido

- **`answering/domain/` (nuevo, Java puro, sin anotaciones de framework):**
  - `EstadoCritico` — value object. Campo `Integer intentosParaQueDejeDeSerCriticoDisponible`; método `actualizar(boolean respuestaCorrecta)` (incorrecta → 3, correcta → decrementa si > 0). Misma lógica que `conteoDeCritico` del modelo viejo, pero aislada como pieza reutilizable — candidata natural para el futuro refactor a SM-2 (memoria del proyecto).
  - `PreguntaSimpleParaResponder` — campos `Long id`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(boolean respuestaDelUsuario)`: **preserva tal cual** el comportamiento incompleto actual — confía directamente en el booleano que manda el cliente, sin comparar contra ningún dato almacenado; delega en `estadoCritico.actualizar(...)` y devuelve la corrección.
  - `VerdaderoOFalsoParaResponder` — campos `Long id`, `Boolean respuestaVerdadera`, `EstadoCritico estadoCritico`. Método `Boolean verificarRespuesta(boolean respuestaDelUsuario)`: compara `respuestaVerdadera == respuestaDelUsuario`, delega en `estadoCritico.actualizar(...)`.
- **`answering/application/port/out/` (nuevo):** `PreguntaSimpleParaResponderPort` y `VerdaderoOFalsoParaResponderPort` — cada uno con `findById(Long): Optional<Xxx>` y `save(Xxx): Xxx`.
- **`answering/application/port/in/` y `application/service/` (nuevo):** `VerificarRespuestaPreguntaSimpleUseCase`/`Service` y `VerificarRespuestaVerdaderoOFalsoUseCase`/`Service` — cada uno resuelve vía su port (`BussinesException` si no existe), llama `verificarRespuesta(dto.respuestaBooleana())` sobre el objeto de dominio, guarda el resultado vía `save`, devuelve el booleano.
- **`answering/infrastructure/persistence/adapter/` (nuevo):** `PreguntaSimpleParaResponderAdapter` y `VerdaderoOFalsoParaResponderAdapter` — implementan los ports inyectando **directamente** `content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository`/`VerdaderoOFalsoJpaRepository` (specs 01-02, reuso cross-slice deliberado y acotado a infraestructura). Convierten `PreguntaSimpleEntity`/`VerdaderoOFalsoEntity` (de `content`) ↔ el objeto de dominio de `answering`, conversión inline en el propio adapter (sin mapper separado, son 2-3 campos).
- **`answering/infrastructure/controller/VerificarRespuestaController` (nuevo):** expone `POST /questions/verify`. Dispatch por `respuestaDelUsuario.tipoDePregunta()`: `PREGUNTA_SIMPLE` → `VerificarRespuestaPreguntaSimpleUseCase`; `VERDADERO_FALSO` → `VerificarRespuestaVerdaderoOFalsoUseCase`; cualquier otro tipo → inyecta `PreguntaService` (viejo) y delega en `verifyResponse` (fallback, sin cambios). Se elimina el método `verifyRequestForUser` de `PreguntaController`.
- **Tests:** paridad de corrección para `PREGUNTA_SIMPLE` (incluida la preservación explícita del comportamiento incompleto: respuesta "correcta" según el booleano del cliente, sin importar el contenido real) y `VERDADERO_FALSO` (comparación real) contra `PreguntaService.verifyResponse` (viejo); actualización del contador de crítico en ambos sentidos (incorrecta→3, correcta→decrementa si >0, correcta con contador en 0 se mantiene en 0); not-found (`BussinesException`) para ambos tipos; no regresión de los 4 tipos no migrados vía el fallback.

### Explícitamente NO incluido

- `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE` en `/questions/verify` — siguen en `PreguntaService.verifyResponse` (viejo) tal cual.
- Relocar `fetch`/`fetch-full` (spec 11) o `random-ids`/`critical-ids` (spec 12) al slice `answering/` — quedan en `content/` tal cual; el desvío respecto a `ARQUITECTURA.md` ya documentado en esos specs no se corrige acá, solo se corta con la deuda nueva desde este spec en adelante.
- Corregir la lógica incompleta de `PreguntaSimple.laRespuestaEsCorrecta` (ignora `respuestaEstablecida`) — se preserva tal cual, decisión ya tomada.
- Implementar SM-2 / repetición espaciada — `EstadoCritico` queda aislado para facilitarlo a futuro, pero no se implementa en este spec.
- `POST /questions/inverse` — queda para un spec futuro.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.

---

## Modelo de datos

### Reutilizado tal cual (sin cambios)

- `content.infrastructure.persistence.entity.PreguntaSimpleEntity` / `PreguntaSimpleJpaRepository` (spec 01) — inyectados directo desde `answering.infrastructure.persistence.adapter.PreguntaSimpleParaResponderAdapter`, sin pasar por `PreguntaSimpleRepositoryPort`/`PreguntaSimpleMapper`/`content.domain.PreguntaSimple` de `content`.
- `content.infrastructure.persistence.entity.VerdaderoOFalsoEntity` / `VerdaderoOFalsoJpaRepository` (spec 02) — mismo criterio.
- `com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO(Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana, ...)` — DTO de request, reutilizado tal cual (mismo criterio que specs 11/12 con `ObtenerPreguntaDTO`/`PostPreguntaDTO`: DTO de la raíz del proyecto, no propiedad de ningún slice).
- `com.lorenzomar3.AQ.exception.BussinesException`, `com.lorenzomar3.AQ.model.TipoAResponder` — mismo uso estándar.
- `Service.PreguntaService.verifyResponse` (viejo) — se reutiliza tal cual como fallback para los 4 tipos no migrados y como baseline de los tests de paridad.

### `answering/domain/` (nuevo, Java puro — sin `@Entity`, `@JsonView` ni imports de Spring/JPA/Jackson)

```java
public class EstadoCritico {
    private Integer intentosParaQueDejeDeSerCriticoDisponible;

    public void actualizar(boolean respuestaCorrecta) {
        if (!respuestaCorrecta) {
            intentosParaQueDejeDeSerCriticoDisponible = 3;
        } else if (intentosParaQueDejeDeSerCriticoDisponible != 0) {
            intentosParaQueDejeDeSerCriticoDisponible--;
        }
    }
}

public class PreguntaSimpleParaResponder {
    private Long id;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(boolean respuestaDelUsuario) {
        estadoCritico.actualizar(respuestaDelUsuario); // preserva el comportamiento incompleto: confía en el booleano del cliente
        return respuestaDelUsuario;
    }
}

public class VerdaderoOFalsoParaResponder {
    private Long id;
    private Boolean respuestaVerdadera;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(boolean respuestaDelUsuario) {
        boolean esCorrecta = respuestaVerdadera == respuestaDelUsuario;
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
```

Todas con getters/setters vía Lombok `@Getter`/`@Setter` (mismo criterio que `content.domain`), sin comportamiento adicional fuera del mostrado.

### `answering/application/port/out/` (nuevo)

- **`PreguntaSimpleParaResponderPort`** — `Optional<PreguntaSimpleParaResponder> findById(Long id)`; `PreguntaSimpleParaResponder save(PreguntaSimpleParaResponder pregunta)`.
- **`VerdaderoOFalsoParaResponderPort`** — mismos métodos con `VerdaderoOFalsoParaResponder`.

### `answering/application/port/in/` y `application/service/` (nuevo)

- **`VerificarRespuestaPreguntaSimpleUseCase`** — `Boolean verificar(RespuestaDePreguntaDTO respuesta)`.
- **`VerificarRespuestaPreguntaSimpleService`** — implementa el puerto; inyecta `PreguntaSimpleParaResponderPort`; resuelve `findById(respuesta.idPregunta())` (`BussinesException` si no existe); llama `preguntaSimpleParaResponder.verificarRespuesta(respuesta.respuestaBooleana())`; guarda vía `save`; devuelve el booleano.
- **`VerificarRespuestaVerdaderoOFalsoUseCase`** — `Boolean verificar(RespuestaDePreguntaDTO respuesta)`.
- **`VerificarRespuestaVerdaderoOFalsoService`** — mismo patrón con `VerdaderoOFalsoParaResponderPort`.

### `answering/infrastructure/persistence/adapter/` (nuevo)

- **`PreguntaSimpleParaResponderAdapter`** — implementa `PreguntaSimpleParaResponderPort`; inyecta `PreguntaSimpleJpaRepository` (de `content`); `findById` mapea `PreguntaSimpleEntity` → `PreguntaSimpleParaResponder` (`id`, `estadoCritico` construido desde `entity.getIntentosParaQueDejeDeSerCriticoDisponible()`); `save` mapea de vuelta y persiste solo el campo de contador (lee la entity de nuevo, actualiza `intentosParaQueDejeDeSerCriticoDisponible`, guarda).
- **`VerdaderoOFalsoParaResponderAdapter`** — análogo, agrega `respuestaVerdadera` al mapeo de ida (no se escribe de vuelta, es de solo lectura para este flujo).

### `answering/infrastructure/controller/VerificarRespuestaController` (nuevo)

- `POST /questions/verify`: dispatch por `respuestaDelUsuario.tipoDePregunta()` — `PREGUNTA_SIMPLE` → `verificarRespuestaPreguntaSimpleUseCase.verificar(...)`; `VERDADERO_FALSO` → `verificarRespuestaVerdaderoOFalsoUseCase.verificar(...)`; resto → `preguntaService.verifyResponse(...)` (viejo, inyectado igual que hacen `TemarioController`/`PreguntaController` con sus fallbacks). Devuelve `ResponseEntity<Boolean>`, mismo contrato exacto que hoy.

### `Controller/PreguntaController` (modificado)

- Se elimina el método `verifyRequestForUser` y el `@PostMapping("/questions/verify")` correspondiente — el endpoint pasa a vivir enteramente en `VerificarRespuestaController`.

### Sin cambios de esquema

Ninguna tabla ni columna nueva — se reutilizan `pregunta`, `pregunta_simple` y `verdadero_o_falso` tal cual.

---

## Plan de implementación

1. **Dominio de `answering`.** Crear `EstadoCritico`, `PreguntaSimpleParaResponder`, `VerdaderoOFalsoParaResponder` en `answering/domain/`, con los campos y métodos definidos en el modelo de datos. Java puro, sin dependencias de Spring/JPA/Jackson.
2. **Tests de dominio.** Tests unitarios (sin Spring) de `EstadoCritico.actualizar` (incorrecta→3, correcta→decrementa si >0, correcta con contador en 0 se mantiene en 0) y de `verificarRespuesta` en ambos objetos de dominio (incluida la preservación explícita del comportamiento incompleto de `PreguntaSimpleParaResponder`).
3. **Ports de salida.** Crear `PreguntaSimpleParaResponderPort` y `VerdaderoOFalsoParaResponderPort` en `answering/application/port/out/`.
4. **Adapters.** Crear `PreguntaSimpleParaResponderAdapter` y `VerdaderoOFalsoParaResponderAdapter` en `answering/infrastructure/persistence/adapter/`, inyectando `PreguntaSimpleJpaRepository`/`VerdaderoOFalsoJpaRepository` de `content` (specs 01-02) y mapeando manualmente contra `PreguntaSimpleEntity`/`VerdaderoOFalsoEntity`.
5. **Use case y service — `PreguntaSimple`.** Crear `VerificarRespuestaPreguntaSimpleUseCase`/`Service` en `answering/application/port/in`/`application/service`, inyectando `PreguntaSimpleParaResponderPort`, lanzando `BussinesException` si no existe.
6. **Use case y service — `VerdaderoOFalso`.** Mismo patrón con `VerificarRespuestaVerdaderoOFalsoUseCase`/`Service`.
7. **Test de paridad — `PreguntaSimple`.** Comparar `VerificarRespuestaPreguntaSimpleService.verificar` contra `PreguntaService.verifyResponse` (viejo): mismo booleano de corrección devuelto, mismo valor final de `intentosParaQueDejeDeSerCriticoDisponible` persistido en BD, para respuesta correcta e incorrecta.
8. **Test de paridad — `VerdaderoOFalso`.** Mismo criterio, cubriendo además el caso de comparación real (`respuestaVerdadera` true/false contra `respuestaBooleana` true/false, las 4 combinaciones).
9. **Tests de not-found.** Id inexistente en ambos Use Cases nuevos → `BussinesException`.
10. **Controller nuevo.** Crear `VerificarRespuestaController` en `answering/infrastructure/controller/`, con el dispatch por `tipoDePregunta` definido en el modelo de datos (2 tipos migrados + fallback a `preguntaService.verifyResponse` para el resto).
11. **Test de no regresión — tipos no migrados.** Para `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`: `POST /questions/verify` a través del controller nuevo sigue devolviendo exactamente lo mismo que antes de este spec.
12. **Limpiar `PreguntaController`.** Eliminar el método `verifyRequestForUser` y su `@PostMapping("/questions/verify")` — el endpoint ya vive solo en `VerificarRespuestaController`.
13. **Verificación final.** Correr `./mvnw test` (lo corre el usuario) y probar contra `AQ-SIMPLE-FRONT`: responder una `PreguntaSimple` (flujo real) confirmando que la corrección y el contador de "crítico" se comportan igual que antes; confirmar que el resto de endpoints (`fetch(-full)`, `random-ids`, `critical-ids`, CRUD de `questions`/`issues`) sigue funcionando sin cambios.

---

## Criterios de aceptación

- [x] Existen `EstadoCritico`, `PreguntaSimpleParaResponder`, `VerdaderoOFalsoParaResponder` en `answering/domain/`, Java puro, sin anotaciones de framework.
- [x] `EstadoCritico.actualizar` replica exactamente `conteoDeCritico` del modelo viejo: incorrecta → `3`; correcta → decrementa si `> 0`; correcta con `0` se mantiene en `0`.
- [x] `PreguntaSimpleParaResponder.verificarRespuesta` preserva el comportamiento incompleto actual: la corrección devuelta es directamente el booleano recibido, sin comparar contra ningún dato almacenado.
- [x] `VerdaderoOFalsoParaResponder.verificarRespuesta` devuelve `true` si y solo si `respuestaVerdadera == respuestaDelUsuario`.
- [x] Existen `PreguntaSimpleParaResponderPort`/`VerdaderoOFalsoParaResponderPort` en `answering/application/port/out/`, implementados por adapters que reutilizan `PreguntaSimpleJpaRepository`/`VerdaderoOFalsoJpaRepository` de `content` (specs 01-02) sin pasar por los ports/domain/services de `content`.
- [x] Existen `VerificarRespuestaPreguntaSimpleUseCase`/`Service` y `VerificarRespuestaVerdaderoOFalsoUseCase`/`Service`, que lanzan `BussinesException` si el id no existe.
- [x] `POST /questions/verify` para una `PreguntaSimple` existente devuelve el mismo booleano que el camino viejo (`PreguntaService.verifyResponse`) y persiste el mismo valor de `intentosParaQueDejeDeSerCriticoDisponible`.
- [x] `POST /questions/verify` para una `VerdaderoOFalso` existente devuelve el mismo booleano que el camino viejo para las 4 combinaciones de `respuestaVerdadera`/`respuestaBooleana`, y persiste el mismo valor de `intentosParaQueDejeDeSerCriticoDisponible`.
- [x] `POST /questions/verify` con un id inexistente de tipo `PREGUNTA_SIMPLE` o `VERDADERO_FALSO` lanza `BussinesException`.
- [x] `POST /questions/verify` para `SELECCION_UNICA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE` sigue devolviendo exactamente lo mismo que antes de este spec (camino viejo intacto vía `VerificarRespuestaController`).
- [x] `PreguntaController` ya no expone `POST /questions/verify` — el endpoint vive únicamente en `VerificarRespuestaController` (`answering/infrastructure/controller/`).
- [x] El resto de endpoints (`fetch(-full)`, `random-ids`, `critical-ids`, CRUD de `questions`/`issues`) sigue funcionando sin cambios.
- [x] Ningún archivo bajo `answering/` importa una clase de `content.application` o `content.domain` — solo se reutilizan `content.infrastructure.persistence.entity.*` y `*.repository.*JpaRepository` (specs 01-02) desde los adapters de `answering`.
- [x] Existen tests cubriendo: lógica pura de `EstadoCritico` y de ambos objetos de dominio, paridad de `verify` para ambos tipos migrados, not-found para ambos, y no regresión de los 4 tipos no migrados.
- [x] `./mvnw test` corre completo y pasa — **verificado por el usuario.**
- [x] Verificación manual contra `AQ-SIMPLE-FRONT`: responder una `PreguntaSimple` desde el flujo real, confirmando que la corrección y el contador de "crítico" se comportan igual que antes. **(verificado por el usuario)**
- [x] `PreguntaService.verifyResponse` (viejo) no fue eliminado; sigue sirviendo como fallback para los 4 tipos no migrados y baseline de los tests de paridad.

---

## Decisiones tomadas y descartadas

- **Se inaugura el slice `answering/` en este spec, en vez de seguir agregando todo a `content/` (como specs 11 y 12).**
  Confirmado con el usuario tras detectar que `answering/` no existía en el código pese a estar definido en `ARQUITECTURA.md`, y que `POST /questions/verify` es literalmente el ejemplo de ese documento (`ResponderPreguntaUseCase`). Descartado: seguir el patrón de specs 11/12 y migrar `verify` dentro de `content/` — se habría profundizado un desvío ya documentado como riesgo en spec 11 ("Coexistencia de dos formas de servir..."), justo en la pieza (contador de "crítico") que el usuario ya identificó como candidata a reescribirse con SM-2 (memoria del proyecto: "Sugerencia SM-2 para críticos").

- **`answering` no importa `application`/`domain` de `content`, pero sí reutiliza `Entity`/`JpaRepository` de `content.infrastructure.persistence` directamente desde sus propios adapters.**
  Confirmado con el usuario, como punto medio explícito entre la letra estricta de `ARQUITECTURA.md` ("answering nunca importa clases de content") y el pragmatismo de no duplicar el mapeo JPA de las mismas tablas (`pregunta_simple`, `verdadero_o_falso`) con un segundo par de `@Entity`. Justificación: el riesgo que la regla busca evitar (acoplar el futuro refactor de "crítico"/SM-2 a los cambios internos de `content`) vive en la capa de `application`/`domain`, no en qué clase JPA lee una columna — esa capa queda 100% aislada. Descartado: duplicar `Entity`+`JpaRepository` propios en `answering/infrastructure/persistence/` — se evaluó como el cumplimiento más estricto de la regla, pero sin beneficio real de flexibilidad futura, solo trabajo mecánico repetido.

- **Se agrega comportamiento real a los objetos de dominio de `answering` (`verificarRespuesta`, `EstadoCritico.actualizar`), rompiendo el patrón anémico usado hasta ahora en `content.domain`.**
  Confirmado con el usuario. Justificación: la lógica de corrección + contador de crítico es comportamiento de dominio genuino (no solo mapeo de campos DTO→entidad como en los `Editar*Service` de `content`), y aislarla en el dominio es precisamente lo que facilita el futuro reemplazo por SM-2 sin tocar `application`/`infrastructure`.

- **`EstadoCritico` se modela como una clase separada (value object), en vez de repetir la lógica del contador inline en cada uno de los dos objetos de dominio.**
  Justificación: a diferencia de `TIPOS_CONTENEDOR` (spec 12, duplicado deliberadamente por no ser abstracción prematura), acá la lógica es idéntica bit a bit entre los dos tipos y no hay variación esperada entre ellos — es duplicación real, no una abstracción prematura sobre algo que podría divergir.

- **Se preserva tal cual el comportamiento incompleto de `PreguntaSimple.laRespuestaEsCorrecta`** (ignora `respuestaEstablecida`, confía en el booleano que manda el cliente).
  Confirmado con el usuario ("Ignora el comportamiento raro. Es lógica incompleta"). Mismo criterio de paridad exacta ya usado en specs 07-10 (no se corrigen comportamientos de negocio fuera de un leak de dato sensible, que no es el caso acá).

- **Dispatch por tipo dentro del controller nuevo (`if/else` o `Map` sobre `tipoDePregunta` del DTO), un Use Case por tipo migrado, con fallback directo a `PreguntaService.verifyResponse` (viejo) para los 4 tipos restantes.**
  Mismo criterio ya usado en `getQuestion`/`getQuestionFull` (spec 11) y `ObtenerIdsAleatoriosDePreguntasService` (spec 12): el tipo ya viene en el request, no hace falta lookup.

- **`POST /questions/verify` se mueve físicamente a `VerificarRespuestaController` (`answering/infrastructure/controller/`), eliminando el método de `PreguntaController`.**
  Confirmado con el usuario, coherente con haber decidido tratar `answering/` como slice real desde el día uno, en vez de repetir el criterio de spec 12 (`ResponderController` viejo que quedó sin mover, "diferido a un spec de relocation").

- **No se relocan en este spec `fetch`/`fetch-full` (spec 11) ni `random-ids`/`critical-ids` (spec 12) a `answering/`.**
  Descartado por alcance: relocar migraciones ya completas y probadas es trabajo mecánico separado, sin relación directa con `verify`. Queda documentado como deuda explícita (ver Riesgos) en vez de mezclarse con este spec.

- **`Service.PreguntaService` (viejo) no se elimina.**
  Mismo criterio de no tocar código fuera del camino activamente migrado, usado en todos los specs anteriores — sigue sirviendo a los 4 tipos no migrados y como baseline de los tests de paridad.

---

## Riesgos identificados

- **`content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository`/`VerdaderoOFalsoJpaRepository` quedan usados por dos adapters de dos slices distintos** (`content`'s propio adapter y el nuevo de `answering`). Si `content` cambia la firma de esos repositorios (agrega una query method, cambia el nombre), rompe silenciosamente a `answering` también — el acoplamiento real existe a nivel de infraestructura, aunque `application`/`domain` de `answering` queden aislados.
  *Mitigación:* ninguna adicional en este spec — es el costo aceptado explícitamente por el usuario a cambio de no duplicar el mapeo JPA; los tests de paridad de este spec y los de specs 01-02 actúan como red de contención si algo se rompe.

- **La conversión `Entity` ↔ objeto de dominio de `answering` es manual e inline en cada adapter, sin un mapper compartido con `content.infrastructure.persistence.mapper`.** Si `PreguntaSimpleEntity`/`VerdaderoOFalsoEntity` ganan un campo nuevo relevante para "responder" en el futuro, hay que recordar actualizar el mapeo en `answering` por separado — no hay ningún mecanismo que lo fuerce.
  *Mitigación:* ninguna en este spec — mismo riesgo de "registro en múltiples puntos" ya aceptado en el resto de la arquitectura (ver `CLAUDE.md`, sección de tipos de pregunta nuevos).

- **La superficie REST de "preguntas" queda repartida en 3 controllers, en 2 paquetes distintos.** `PreguntaController` (`Controller/`, CRUD + fetch), `ResponderController` (`Controller/`, hexagonal por dentro desde spec 12 pero sin relocar) y ahora `VerificarRespuestaController` (`answering/infrastructure/controller/`, nuevo). Alguien buscando "dónde vive `/questions/verify`" ya no la encuentra en `PreguntaController` como esperaría por convención con el resto de `/questions/*`.
  *Mitigación:* ninguna adicional — se documenta acá y en `ARQUITECTURA.md` como deuda de relocation ya conocida (spec 12 la señaló para `ResponderController`, este spec la extiende).

- **`EstadoCritico` está diseñado pensando en facilitar un futuro SM-2, pero nada en este spec valida que su forma actual (un único `Integer`) sea la correcta para ese algoritmo** (que típicamente necesita más estado: fecha de próxima revisión, factor de facilidad, historial). Es una apuesta de diseño, no una garantía.
  *Mitigación:* ninguna — se acepta que el refactor a SM-2, cuando llegue, probablemente necesite ampliar o reemplazar `EstadoCritico`; el valor de este spec es que ese cambio quedaría contenido en `answering/domain/`, sin tocar `content`.

- **`AResponder.tipo` en el modelo JPA viejo sigue siendo un campo mantenido a mano, no un discriminator de JPA** (mismo riesgo estructural ya documentado en specs anteriores) — si estuviera desincronizado, el dispatch por `tipoDePregunta` del request en `VerificarRespuestaController` podría enviar la verificación al Use Case nuevo equivocado.
  *Mitigación:* ninguna adicional — mismo riesgo estructural ya aceptado en toda la migración.
