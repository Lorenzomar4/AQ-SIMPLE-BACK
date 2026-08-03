# Spec 19 — Reubicar las piezas flotantes de la arquitectura por capas (`TipoAResponder`, `BussinesException`, `Controller/`, `dto/newDto/`, `projections/`) en los slices hexagonales

**Estado:** Implementado — `./mvnw test` y `./mvnw package` en verde y verificación manual contra `AQ-SIMPLE-FRONT` completada
**Dependencias:** Spec 18 (deja `model/AResponder/**` completamente borrado; lo único que queda de la arquitectura por capas vieja son 5 paquetes huérfanos: `model/TipoAResponder.java`, `exception/BussinesException.java`, `Controller/**` (3 archivos), `dto/newDto/**` (32 archivos), `projections/**` (3 archivos)).
**Fecha:** 2026-08-02
**Objetivo:** Reubicar mecánicamente esas 5 piezas flotantes dentro de `content/` y `answering/` (según a qué slice pertenecen), sin rediseñar su rol ni cambiar comportamiento — mismo criterio de "deuda aceptada, no se corrige retroactivamente" que `ARQUITECTURAV3.md` ya documenta para el patrón `UseCase/Service` de `content/`.

---

## Alcance

### Incluido

- **`content/api/TipoAResponder.java` (nuevo)** — movido desde `model/TipoAResponder.java`, mismos valores del enum (`PREGUNTA_SIMPLE`, `VERDADERO_FALSO`, `SELECCION_UNICA`, `TEMA`, `SUBTEMA`, `OPCION_MULTIPLE`, `DESPLEGABLE_COMPARTIDO`, `DESPLEGABLE_INDEPENDIENTE`, `CUESTIONARIO`), sin cambios. `content/domain/AResponder.tipo` pasa a depender de `content.api.TipoAResponder` — **excepción documentada** a la tabla estricta de `ARQUITECTURAV3.md` (`domain/` solo debería depender de "nada" + `shared/domain/`), justificada porque es un value type sin comportamiento ni acoplamiento a framework.
- **`shared/exception/BussinesException.java` (nuevo)** — movido desde `exception/BussinesException.java`, sin cambios de comportamiento (primer archivo del paquete `shared/`, coincide con el destino ya nombrado explícitamente en el diagrama de `ARQUITECTURAV3.md`).
- **`content/infrastructure/controller/{TemarioController,PreguntaController,ResponderController}.java`** — movidos desde `Controller/**`, sin cambios de lógica interna, solo paquete + imports. Los 3 quedan en `content/` (incluido `ResponderController`, desviación documentada del diagrama objetivo de `ARQUITECTURAV3.md`, que lo ubicaba en `answering/`, porque sus dos `UseCase` — `ObtenerIdsAleatoriosDePreguntasUseCase`, `ObtenerIdsCriticosUseCase` — viven físicamente en `content.application.port.in`).
- **`content/infrastructure/controller/dto/*.java` (31 archivos)** — movidos desde `dto/newDto/**`, todos menos `RespuestaDePreguntaDTO`. Mudanza mecánica: mismo nombre, mismos campos, sin reclasificar si alguno "debería" ser un `Command`/`View` interno.
- **`answering/infrastructure/controller/dto/RespuestaDePreguntaDTO.java`** — movido desde `dto/newDto/RespuestaDePreguntaDTO.java` (único DTO consumido por `answering`).
- **`content/infrastructure/controller/projection/*.java` (3 archivos)** — movidos desde `projections/**`: `QuestionnaireItem`, `AResponderIdTipoProjection`, `IssueOrQuestionnaireProjection`. Esta última no tiene callers detectados (código muerto preexistente) — se mueve igual, sin decisión de borrado en este spec.
- **`src/test/java/.../Controller/PreguntaControllerUpdateQuestionIntegrationTest.java`** → `src/test/java/.../content/infrastructure/controller/`, mismo criterio: mudanza de paquete, mirror de `src/main`.
- **Actualización de imports en todo archivo que referencia cualquiera de las 5 piezas movidas** (~50 para `TipoAResponder`, ~48 para `BussinesException`, ~93 para `dto.newDto.*`, un puñado para `projections.*`) — solo cambia la ruta de import, cero cambios de lógica.
- **Borrado de los paquetes vacíos resultantes:** `model/`, `Controller/`, `dto/newDto/`, `projections/`, `exception/` — confirmado por grep que quedan sin ningún archivo tras los pasos anteriores.

### Explícitamente NO incluido

- **Ningún rediseño de a qué sub-paquete "debería" pertenecer cada DTO** (p. ej. si `PostPreguntaDTO` debería ser un `Command` de `application/` en vez de vivir en `infrastructure/controller/dto/`) — mismo criterio de deuda aceptada que `ARQUITECTURAV3.md` ya documenta para `content/`.
- **Mover `ObtenerIdsAleatoriosDePreguntasUseCase`/`ObtenerIdsCriticosUseCase` a `answering/`** — quedan en `content.application.port.in`, sin tocar.
- **Aplicar "package-private siempre que Java lo permita"** a los controllers movidos — siguen `public`.
- **Borrar `IssueOrQuestionnaireProjection.java`** pese a no tener callers — se mueve tal cual.
- Cualquier cambio de esquema de base de datos.
- Cambios en `AQ-SIMPLE-FRONT`.
- Tests nuevos — se actualizan los imports de los tests existentes, no se agrega cobertura nueva (cambio puramente mecánico).

---

## Modelo de datos

Este spec no introduce datos nuevos — es una reubicación de paquetes. Se muestra la forma final de las piezas clave para dejar sin ambigüedad los nombres de paquete resultantes.

```java
// content/api/TipoAResponder.java (movido desde model/TipoAResponder.java, sin cambios de valores)
package com.lorenzomar3.AQ.content.api;

public enum TipoAResponder {
    PREGUNTA_SIMPLE, VERDADERO_FALSO, SELECCION_UNICA, TEMA, SUBTEMA,
    OPCION_MULTIPLE, DESPLEGABLE_COMPARTIDO, DESPLEGABLE_INDEPENDIENTE, CUESTIONARIO
}
```

```java
// shared/exception/BussinesException.java (movido desde exception/BussinesException.java, sin cambios)
package com.lorenzomar3.AQ.shared.exception;

// contenido idéntico al actual — solo cambia el package
public class BussinesException extends RuntimeException { ... }
```

```java
// content/domain/AResponder.java — único cambio: el import
import com.lorenzomar3.AQ.content.api.TipoAResponder;   // antes: com.lorenzomar3.AQ.model.TipoAResponder
```

```java
// content/infrastructure/controller/PreguntaController.java — cambia el package + los imports,
// cero cambios en el cuerpo de los métodos
package com.lorenzomar3.AQ.content.infrastructure.controller;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
// ... resto de imports de content.application.port.in / query / command, sin cambios
```

```java
// content/infrastructure/controller/dto/PostPreguntaDTO.java (ejemplo representativo de los 31 DTOs movidos)
package com.lorenzomar3.AQ.content.infrastructure.controller.dto;   // antes: com.lorenzomar3.AQ.dto.newDto

// contenido idéntico al actual — solo cambia el package y los imports internos de TipoAResponder
```

```java
// answering/infrastructure/controller/dto/RespuestaDePreguntaDTO.java
package com.lorenzomar3.AQ.answering.infrastructure.controller.dto;   // antes: com.lorenzomar3.AQ.dto.newDto
```

```java
// content/infrastructure/controller/projection/QuestionnaireItem.java (y AResponderIdTipoProjection, IssueOrQuestionnaireProjection)
package com.lorenzomar3.AQ.content.infrastructure.controller.projection;   // antes: com.lorenzomar3.AQ.projections
```

Estructura final de paquetes tocados:

```
content/
├── api/
│   └── TipoAResponder.java                    (nuevo, movido)
└── infrastructure/
    └── controller/
        ├── TemarioController.java             (movido)
        ├── PreguntaController.java            (movido)
        ├── ResponderController.java           (movido)
        ├── dto/                                (nuevo paquete, 31 archivos movidos)
        └── projection/                         (nuevo paquete, 3 archivos movidos)

answering/
└── infrastructure/
    └── controller/
        └── dto/
            └── RespuestaDePreguntaDTO.java     (movido)

shared/
└── exception/
    └── BussinesException.java                  (nuevo, movido)
```

---

## Plan de implementación

1. **Crear `shared/exception/BussinesException.java`** (contenido idéntico al actual). Actualizar los ~48 imports en `src/main` y `src/test` de `com.lorenzomar3.AQ.exception.BussinesException` → `com.lorenzomar3.AQ.shared.exception.BussinesException`. Borrar `exception/BussinesException.java` y el paquete `exception/` vacío. Compilar — debe quedar limpio.
2. **Crear `content/api/TipoAResponder.java`** (mismos valores del enum). Actualizar los ~50 imports de `com.lorenzomar3.AQ.model.TipoAResponder` → `com.lorenzomar3.AQ.content.api.TipoAResponder`, incluyendo `content/domain/AResponder.java` (excepción documentada: domain pasa a depender de api del propio slice). Borrar `model/TipoAResponder.java` y el paquete `model/` vacío. Compilar.
3. **Crear `content/infrastructure/controller/projection/`** con los 3 archivos movidos desde `projections/**` (`QuestionnaireItem`, `AResponderIdTipoProjection`, `IssueOrQuestionnaireProjection`). Actualizar sus importers (`AResponderJpaRepository`, `Controller/TemarioController` — ver paso 5 — y los tests de `AResponderJpaRepositoryIntegrationTest`). Borrar `projections/**` viejo. Compilar.
4. **Crear `content/infrastructure/controller/dto/`** con los 31 DTOs movidos desde `dto/newDto/**` (todos menos `RespuestaDePreguntaDTO`), y `answering/infrastructure/controller/dto/RespuestaDePreguntaDTO.java`. Actualizar los ~93 imports de `com.lorenzomar3.AQ.dto.newDto.*`, repartiéndolos entre los dos paquetes nuevos según a qué DTO referencian. Borrar `dto/newDto/**` viejo. Compilar.
5. **Mover `Controller/{TemarioController,PreguntaController,ResponderController}.java`** → `content/infrastructure/controller/`, actualizando su propio `package` — sus imports ya deberían apuntar a los paquetes nuevos creados en los pasos 1-4. Mover `src/test/.../Controller/PreguntaControllerUpdateQuestionIntegrationTest.java` → `src/test/.../content/infrastructure/controller/`. Borrar `Controller/**` viejo. Compilar.
6. **Grep de verificación**: confirmar que ningún archivo bajo `src/main` ni `src/test` referencia `com.lorenzomar3.AQ.model.*`, `com.lorenzomar3.AQ.exception.*`, `com.lorenzomar3.AQ.Controller.*`, `com.lorenzomar3.AQ.dto.newDto.*` ni `com.lorenzomar3.AQ.projections.*`.
7. **Verificación.** `./mvnw test` (lo corre el usuario) — debe compilar y pasar completo, sin tests nuevos (comportamiento sin cambios). `./mvnw package`. Verificación manual contra `AQ-SIMPLE-FRONT`: ejercitar al menos un endpoint de cada controller movido (`GET /questionnaires`, `POST /questions/fetch`, `POST /questions/verify`, `POST /questions/random-ids`) para confirmar que el redeploy de paquetes no rompió el mapeo de Spring MVC ni la serialización Jackson.

A diferencia de spec 18, cada paso de este plan mueve **un solo tipo de pieza a la vez** (no cambia la forma de ningún campo), así que en principio cada paso debería dejar el proyecto compilando limpio antes de pasar al siguiente — sin la unidad de cambio forzada que tuvo spec 18.

---

## Criterios de aceptación

- [x] `content/api/TipoAResponder.java` existe con los mismos 9 valores que el enum original; `model/TipoAResponder.java` no existe.
- [x] `content/domain/AResponder.tipo` es de tipo `content.api.TipoAResponder`.
- [x] `shared/exception/BussinesException.java` existe con el mismo comportamiento que el original; `exception/BussinesException.java` no existe.
- [x] `content/infrastructure/controller/TemarioController.java`, `PreguntaController.java` y `ResponderController.java` existen, con el mismo comportamiento HTTP (mismos paths, mismos métodos, mismos códigos de respuesta) que antes de moverlos; `Controller/**` no existe.
- [x] `content/infrastructure/controller/dto/` contiene los DTOs movidos desde `dto/newDto/**`; `answering/infrastructure/controller/dto/RespuestaDePreguntaDTO.java` existe con el DTO restante. **Nota:** el conteo real fue 33 DTOs (no 31 como decía el spec) + `RespuestaDePreguntaDTO` = 34 archivos totales en el `dto/newDto/**` original — desfase en el conteo del texto del spec, no una decisión de diseño; se movieron todos los archivos reales según el criterio "todos menos `RespuestaDePreguntaDTO`".
- [x] `content/infrastructure/controller/projection/` contiene `QuestionnaireItem.java`, `AResponderIdTipoProjection.java` e `IssueOrQuestionnaireProjection.java`.
- [x] `dto/newDto/**` y `projections/**` no existen.
- [x] `model/`, `Controller/`, `dto/newDto/`, `projections/` y `exception/` no existen como paquetes en el repositorio.
- [x] Ningún archivo bajo `src/main` ni `src/test` referencia `com.lorenzomar3.AQ.model.*`, `com.lorenzomar3.AQ.exception.*`, `com.lorenzomar3.AQ.Controller.*`, `com.lorenzomar3.AQ.dto.newDto.*` ni `com.lorenzomar3.AQ.projections.*` (verificado por grep).
- [x] `src/test/.../content/infrastructure/controller/PreguntaControllerUpdateQuestionIntegrationTest.java` existe (movido); no queda ningún test bajo `src/test/.../Controller/`.
- [x] Ningún test nuevo fue agregado — la suite existente pasa sin modificaciones de aserciones (solo imports/paquetes tocados donde corresponda).
- [x] `./mvnw test` corre completo y pasa — confirmado por el usuario.
- [x] `./mvnw package` compila sin errores — confirmado por el usuario.
- [x] Verificación manual contra `AQ-SIMPLE-FRONT` de al menos un endpoint de cada controller movido — confirmado por el usuario.

---

## Decisiones tomadas

- **Un solo spec, no dividido en Frente A/B** (a diferencia del patrón spec 17→18). El usuario prefirió cerrar de punta a punta la reubicación de las 5 piezas flotantes en una sola unidad de trabajo, aceptando que sea un spec más grande.
- **`TipoAResponder` va a `content/api/`, no a `content/domain/` ni a `shared/domain/`.** Se descartó `shared/domain/` porque la regla de admisión de `ARQUITECTURAV3.md` la excluye explícitamente (depende de "preguntas/cuestionarios"). Se prefirió `content/api/` sobre `content/domain/` porque ya hoy cruza slices (usado en `answering/infrastructure/controller` y en varios DTOs de borde HTTP) y la tabla de dependencias permite que otro slice dependa de la `api/` de otro, no de su `domain/`.
- **Se acepta como excepción documentada que `content/domain/AResponder.tipo` dependa de `content/api/TipoAResponder`**, en vez de duplicar el enum en `domain/` y `api/` con un mapper de conversión. Se descartó la alternativa de dos enums porque agregaba una clase y un mapeo nuevos para un value type sin ningún comportamiento ni acoplamiento real a un framework — el tipo de acoplamiento que la regla de pureza de dominio busca evitar (Spring, JPA, Jackson) no aplica acá.
- **`ResponderController` se queda en `content/infrastructure/controller/`, no se mueve a `answering/`** — desviación explícita del diagrama objetivo de `ARQUITECTURAV3.md` (que lo ubicaba en `answering/`). Se descartó moverlo porque sus dos `UseCase` (`ObtenerIdsAleatoriosDePreguntasUseCase`, `ObtenerIdsCriticosUseCase`) viven físicamente en `content.application.port.in`; moverlo tal cual haría que `answering` dependiera de un paquete interno de `content` (no de `content.api`), violando la regla de dependencia entre slices. Resolver eso "bien" (exponer esos dos casos de uso vía `content.api`) se descartó por ampliar demasiado el alcance de este spec.
- **No se aplica "package-private siempre que Java lo permita"** a los controllers ni a los DTOs/projections movidos — quedan `public` como hoy. Se descartó porque Spring necesita poder instanciarlos/proxearlos igual, y es un cambio de bajo valor práctico que se prefirió no mezclar con esta migración de paquetes.
- **`dto/newDto/**` y `projections/**` se mueven mecánicamente**, sin reclasificar cada archivo según si "debería" ser un `Command`/`View` interno o un DTO de borde HTTP. Se descartó el análisis fino por ser un trabajo de diseño considerablemente mayor (32 archivos), y porque `ARQUITECTURAV3.md` ya acepta ese mismo tipo de deuda para el patrón `UseCase/Service` de `content/`.
- **Los DTOs y projections movidos aterrizan en `content/infrastructure/controller/dto/` y `.../projection/`** (anidados bajo el controller que los consume), no en `content/dto/`/`content/projections/` a nivel raíz del slice — refleja que hoy son objetos de borde HTTP/persistencia, sin mezclarlos con `application/` ni `domain/`.
- **`BussinesException` va a `shared/exception/`** — sin ambigüedad, es el destino que el propio diagrama de `ARQUITECTURAV3.md` ya nombra explícitamente.
- **`IssueOrQuestionnaireProjection.java` (código muerto detectado, sin callers) se mueve tal cual, sin borrarlo** — la limpieza de código muerto no relacionado con esta reubicación queda fuera de alcance, mismo criterio que otras decisiones "mecánicas" de este spec.

---

## Riesgos identificados

- **Volumen de archivos tocados** (~150+ referencias repartidas en 5 grupos de imports) aumenta la superficie de error de un find-and-replace mal dirigido (p. ej. reemplazar `dto.newDto` dentro de un comentario o string en vez de un import). *Mitigación:* el plan hace un grep de verificación dedicado (paso 6) antes de dar el spec por cerrado, y cada paso 1-5 se compila individualmente antes de avanzar al siguiente.
- **La excepción `domain → api` documentada para `TipoAResponder` es una desviación real de la tabla de dependencias de `ARQUITECTURAV3.md`**, no una interpretación laxa de la regla. Si en el futuro el proyecto decide enforcar esa tabla de forma más estricta (p. ej. con un linter de arquitectura tipo ArchUnit), este campo puntual necesitará revisitarse. *Mitigación:* ninguna nueva — queda documentada como excepción consciente, no como deuda oculta.
- **`ResponderController` quedando en `content/` en vez de `answering/` deja una inconsistencia permanente respecto al diagrama de `ARQUITECTURAV3.md`**, que este spec no actualiza. *Mitigación:* ninguna en este spec — se documenta acá como la fuente de verdad más reciente sobre por qué la desviación existe; actualizar el diagrama del documento de arquitectura queda fuera de alcance.
- **Sin "package-private" aplicado, nada impide hoy que `answering` importe directamente `content.infrastructure.controller.dto.PostPreguntaDTO`** u otra clase interna de `content` — la separación entre slices sigue dependiendo de disciplina, no del compilador. *Mitigación:* ninguna nueva — mismo trade-off ya aceptado en el resto del proyecto (ver "Visibilidad por paquete" en `ARQUITECTURAV3.md`, sección que reconoce esta limitación de Java).
