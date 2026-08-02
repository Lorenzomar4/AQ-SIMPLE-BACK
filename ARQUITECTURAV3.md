# Arquitectura objetivo — AQ-SIMPLE-BACK (v3)

> Reemplaza a `ARQUITECTURAV2.md` como documento objetivo. V2 queda como referencia histórica del razonamiento que llevó hasta acá — no se usa para diseñar código nuevo. V1 (`ARQUITECTURA.md`) sigue siendo legacy puro.

## Qué cambia respecto a v2

- Se resuelve la ambigüedad de nombres `Command`/`Query` (ver [Nomenclatura](#nomenclatura-de-application)).
- Se agregan reglas explícitas sobre: transacciones, aggregate roots, validaciones por capa, eventos como hechos, mappers nunca en el dominio, y dependencia cero del dominio respecto a Spring.
- Se endurece la regla de dependencia entre slices: tampoco se puede leer el esquema/tablas de otro slice, ni siquiera con una query nativa.
- Se agregan diagramas simples.
- Se agrega un paso 0 a la migración: cubrir con tests antes de migrar.
- Se documenta explícitamente la deuda existente: **el slice `content/` fue escrito con el patrón `port/in/XxxUseCase` + `application/service/XxxService`, no con `Command/Handler`. Esto queda tal cual está — no se renombra retroactivamente.** El estándar de este documento (`Command/Handler`) aplica a partir de ahora para código nuevo y para el día que se decida migrar `content/`. Ver [Deuda de nomenclatura](#deuda-de-nomenclatura-pendiente).

---

## Patrones aplicados

- **Hexagonal (Ports & Adapters)** — el dominio no depende de ningún framework. Spring, JPA y Jackson viven en la capa de infraestructura.
- **Vertical Slicing** — el código se organiza por capacidad de negocio, no por tipo técnico. Cada slice es autocontenido.
- **CQRS ligero dentro de cada slice** — `application/` separa `command/` (escritura) de `query/` (lectura), sin necesidad de almacenes o modelos de datos distintos.

---

## Principios del proyecto

1. **Alta cohesión** — cada slice representa una capacidad de negocio completa (autoría, respuesta, ...), no una capa técnica.
2. **Bajo acoplamiento** — los slices solo colaboran mediante contratos públicos (`api/`), nunca mediante sus clases internas.
3. **Dominio puro** — el dominio no conoce frameworks: sin `@Entity`, sin `@JsonView`, sin `BeanUtils`, sin anotaciones de Spring de ningún tipo.
4. **Dependencias hacia adentro** — `infrastructure → application → domain`, siempre.
5. **Evolución independiente** — cada slice debe poder cambiar de implementación (BD, mapeo, incluso su modelo interno) sin romper a los demás.
6. **Compartir contratos, no implementación** — si dos slices necesitan colaborar, se comparte una interfaz/DTO (`api/`) o un evento, nunca una entidad, repositorio o mapper interno.
7. **Evitar abstracciones prematuras** — no crear puertos, eventos o capas adicionales hasta que exista una necesidad real y concreta.

---

## Los slices

### `content/` — Autoría
Todo lo relacionado a crear, editar y estructurar el contenido del cuestionario.
Contiene: cuestionarios, temas, subtemas, preguntas de cualquier tipo.

### `answering/` — Respuesta
Todo lo relacionado al flujo de responder: obtener preguntas, verificar respuestas, gestionar críticos.

### Regla de dependencia entre slices

> **Los slices no dependen de la implementación interna de otros slices.** Si necesitan colaborar, lo hacen exclusivamente a través de la **API pública** de ese slice: interfaces expuestas en su paquete `api/`, DTOs de solo lectura, o eventos de dominio. Nunca mediante entidades, repositorios, mappers o adapters de otro slice.

Esto incluye explícitamente:
- No importar `PreguntaEntity`, `PreguntaRepository`, `PreguntaMapper` ni ningún `JpaAdapter` de otro slice.
- **No hacer una query nativa o JPQL propia contra una tabla que "pertenece" a otro slice**, aunque no se importe ninguna clase Java de ese slice. Conocer el nombre de una tabla o columna ajena es acoplamiento igual de real que importar la Entity — solo que más difícil de detectar en un `import`.

`answering` solo conoce lo que `content.api` decide exponer (p. ej. `PreguntaSimpleParaResponderView`, `ObtenerPreguntaSimpleParaResponderQuery`).

---

## Visibilidad por paquete

Dentro de cada slice, solo el paquete `api/` es público (`public`). El resto de las clases (`domain`, `application`, `infrastructure`) deberían ser *package-private* siempre que Java lo permita (esto es limitado entre subpaquetes, pero es la intención a respetar incluso cuando el compilador no lo puede forzar del todo). Así el propio diseño de paquetes ayuda a mantener el límite del slice, y no depende solo de la disciplina del equipo.

---

## Nomenclatura de `application/`

Este es el punto más importante que corrige v3 sobre v2: los nombres `Command`/`Query` eran ambiguos porque podían referirse al DTO, al caso de uso o al handler indistintamente.

**Escritura (`application/command/`):**
- `XxxCommand` — record con los datos de entrada.
- `XxxHandler` — ejecuta el comando. Un método público, normalmente `handle(XxxCommand)`.

**Lectura (`application/query/`):**
- `XxxQuery` — record con los parámetros de entrada (si la consulta no necesita parámetros, se omite).
- `XxxQueryHandler` — ejecuta la consulta. Sufijo `QueryHandler`, no `Handler` a secas, para no confundirlo con un handler de escritura al ojear el paquete.
- `XxxResult` — DTO de salida, cuando el resultado no es trivial (para resultados triviales, ej. devolver directamente una `List<PreguntaView>`, no hace falta envolver en un `Result`).

```java
// application/command/CrearPreguntaCommand.java
public record CrearPreguntaCommand(String enunciado, TipoAResponder tipo, Long temarioId) { }

// application/command/CrearPreguntaHandler.java
public class CrearPreguntaHandler {
    public Long handle(CrearPreguntaCommand command) { ... }
}

// application/query/ObtenerCuestionariosQuery.java
public record ObtenerCuestionariosQuery(String filtroNombre) { }

// application/query/ObtenerCuestionariosQueryHandler.java
public class ObtenerCuestionariosQueryHandler {
    public ObtenerCuestionariosResult handle(ObtenerCuestionariosQuery query) { ... }
}

// application/query/ObtenerCuestionariosResult.java
public record ObtenerCuestionariosResult(List<CuestionarioView> cuestionarios) { }
```

**Contratos públicos en `api/`:** un solo artefacto por capacidad expuesta, nunca una interfaz y su implementación juntas en `api/`.
- Si otro slice necesita **preguntar algo de forma síncrona**, se expone una interfaz con un método (p. ej. `ObtenerPreguntaSimpleParaResponderQuery`). La implementación (`XxxQueryHandler`) vive en `application/query/` y se registra como el bean que satisface esa interfaz — pero eso es un detalle de implementación, no se duplica en `api/`.
- Si otro slice necesita **avisar algo o pedir una mutación**, se expone un `Command` (record) que el slice dueño interpreta, o un evento de dominio si la colaboración es asíncrona.

---

## Estructura de paquetes

```
com/aq/
│
├── shared/                                           ← conceptos realmente universales, sin lógica de negocio
│   ├── domain/
│   │   ├── DomainEvent.java
│   │   └── EntityId.java
│   ├── exception/
│   │   └── BussinesException.java
│   └── util/
│
├── content/
│   ├── api/                                          ← ÚNICO paquete público del slice
│   │   ├── PreguntaView.java
│   │   └── ObtenerPreguntaQuery.java                 ← un solo artefacto, sin Service duplicado
│   │
│   ├── domain/                                       ← Java puro, sin anotaciones de frameworks
│   │   ├── AResponder.java
│   │   ├── Temario.java
│   │   ├── Pregunta.java
│   │   ├── PreguntaSimple.java
│   │   ├── VerdaderoOFalso.java
│   │   ├── SeleccionUnica.java
│   │   ├── OpcionMultiple.java
│   │   ├── DesplegableCompartido.java
│   │   ├── DesplegableIndependiente.java
│   │   ├── FabricaDePreguntas.java
│   │   └── event/
│   │       └── PreguntaCreadaEvent.java
│   │
│   ├── application/
│   │   ├── command/
│   │   │   ├── CrearCuestionarioCommand.java
│   │   │   ├── CrearCuestionarioHandler.java
│   │   │   ├── CrearPreguntaCommand.java
│   │   │   ├── CrearPreguntaHandler.java
│   │   │   ├── EditarTemarioCommand.java
│   │   │   ├── EditarTemarioHandler.java
│   │   │   └── EliminarTemarioHandler.java
│   │   ├── query/
│   │   │   ├── ObtenerCuestionariosQuery.java
│   │   │   ├── ObtenerCuestionariosQueryHandler.java
│   │   │   ├── ObtenerCuestionariosResult.java
│   │   │   └── ObtenerPreguntaQueryHandler.java       ← implementa content.api.ObtenerPreguntaQuery
│   │   └── port/
│   │       └── out/
│   │           ├── TemarioRepositoryPort.java
│   │           └── PreguntaRepositoryPort.java
│   │
│   └── infrastructure/
│       ├── persistence/
│       │   ├── entity/                               ← entidades JPA (@Entity, sin lógica)
│       │   │   ├── AResponderEntity.java
│       │   │   ├── TemarioEntity.java
│       │   │   ├── PreguntaEntity.java
│       │   │   ├── PreguntaSimpleEntity.java
│       │   │   ├── VerdaderoOFalsoEntity.java
│       │   │   └── ... (resto de subclases)
│       │   ├── mapper/                               ← convierten Entity ↔ Domain (manuales o MapStruct)
│       │   │   ├── TemarioMapper.java
│       │   │   └── PreguntaMapper.java
│       │   └── adapter/                              ← implementan los repository ports
│       │       ├── TemarioJpaAdapter.java
│       │       └── PreguntaJpaAdapter.java
│       └── controller/                               ← adaptadores IN: reciben HTTP, llaman use cases
│           ├── TemarioController.java
│           └── PreguntaController.java
│
└── answering/
    ├── api/                                          ← público hacia otros slices (p. ej. statistics futuro)
    │   └── RespuestaVerificadaView.java
    │
    ├── domain/                                       ← Java puro, sin anotaciones de frameworks
    │   ├── Respuesta.java
    │   ├── EstadoCritico.java
    │   └── event/
    │       └── PreguntaRespondidaEvent.java
    │
    ├── application/
    │   ├── command/
    │   │   ├── ResponderPreguntaCommand.java
    │   │   └── ResponderPreguntaHandler.java          ← usa content.api.ObtenerPreguntaQuery, no content.domain
    │   ├── query/
    │   │   ├── ObtenerCriticosQuery.java
    │   │   └── ObtenerCriticosQueryHandler.java
    │   └── port/
    │       └── out/
    │           └── RespuestaRepositoryPort.java        ← puerto propio de answering, solo sobre su propio dato
    │
    └── infrastructure/
        ├── persistence/
        │   └── RespuestaJpaAdapter.java
        └── controller/
            └── ResponderController.java
```

---

## Diagramas

### Dentro de un slice

```
        HTTP
          │
    Controller (infra)
          │
   Command / Query (application)
          │
      Handler (application)
       /        \
      /          \
  Domain        Port out (application)
                    │
              JpaAdapter (infra)
                    │
                 Database
```

### Entre slices

```
+-----------+        usa api/        +-----------+
| answering |  ────────────────────▶ |  content  |
+-----------+                        +-----------+
                                            │
                                            ▼
                                        Database
```

`answering` nunca ve `content/domain`, `content/application` (salvo lo expuesto vía `api/`) ni `content/infrastructure`.

---

## Flujo de dependencias

### Entre slices

```
answering/application/command/ResponderPreguntaHandler
    depende de ──→ content/api/ObtenerPreguntaQuery   (interfaz pública, DTO PreguntaView)
                    nunca de content/domain, content/application ni content/infrastructure

content/application/query/ObtenerPreguntaQueryHandler
    implementa ──→ content/api/ObtenerPreguntaQuery
                    (traduce su modelo interno a PreguntaView antes de responder)
```

`answering` no tiene su propio puerto `out` para "leer preguntas" (el antiguo `PreguntaParaResponderPort` accediendo a BD): esa responsabilidad es de `content`, que la publica en `content.api`. `answering` solo conserva puertos `out` sobre **su propio** dato (`RespuestaRepositoryPort`).

### Colaboración vía eventos (para casos futuros)

Cuando la colaboración no requiere una respuesta síncrona, un evento de dominio es preferible a una llamada directa a la API de otro slice — desacopla en el tiempo y permite múltiples suscriptores sin que el publicador los conozca:

```
answering/domain/event/PreguntaRespondidaEvent
    │
    ▼
(futuro) statistics    — actualiza métricas de aciertos/errores
(futuro) achievements  — evalúa logros desbloqueados
(futuro) notifications — notifica hitos al usuario
```

Hoy no existen esos slices, así que no se crea el mecanismo de eventos todavía (ver principio 7: evitar abstracciones prematuras). Cuando aparezca el primer consumidor real, se introduce un `DomainEvent` en `shared/domain/` y un publicador simple (puede ser `ApplicationEventPublisher` de Spring en la infraestructura, sin filtrarse al dominio).

**Los eventos expresan hechos del negocio, no acciones.** Un evento describe algo que ya ocurrió — se nombra en participio, no en infinitivo/imperativo:

| ✔ Correcto | ✘ Incorrecto |
|---|---|
| `PreguntaRespondidaEvent` | `ResponderPreguntaEvent` |
| `UsuarioRegistradoEvent` | `RegistrarUsuarioEvent` |
| `TemarioCreadoEvent` | `CrearTemarioEvent` |

---

## Reglas de dependencia (las más importantes)

| Capa / concepto | Puede depender de | No puede depender de |
|------|-------------------|----------------------|
| `domain/` | Nada (Java puro), `shared/domain/` | application, infrastructure, frameworks |
| `application/` | `domain/`, `shared/` | `infrastructure/`, Spring, JPA |
| `infrastructure/` | `application/`, `domain/` | Otros slices |
| Un slice completo | La **`api/`** de otro slice, `shared/` | `domain/`, `application/` (fuera de `api/`) o `infrastructure/` de otro slice |

### El dominio nunca depende de Spring

Ni de ningún framework. Concretamente, en `domain/` no debe aparecer nada de esto:

- ❌ `@Autowired`, `@Component`, `@Service`, `@Repository`
- ❌ `@Transactional`
- ❌ `@Entity`, `@Table`, `@Id`, `@OneToMany`, cualquier anotación JPA
- ❌ `@JsonView`, cualquier anotación de Jackson
- ❌ `BeanUtils.copyProperties`
- ❌ `ApplicationEventPublisher`

Si el dominio necesita publicar un evento, expone el evento como valor de retorno o lo acumula en una lista interna que el `Handler` de `application/` lee y publica — el dominio nunca invoca al publicador directamente.

### `@Transactional` vive en los Handlers

`@Transactional` es una preocupación de infraestructura (delimita una transacción de BD), pero por cómo funciona el proxying de Spring, el lugar correcto para anotarlo es el `Handler` en `application/` — nunca en `domain/`, nunca en el `Controller`. Poner `@Transactional` en el Controller expone la transacción a la capa HTTP (mala idea: una petición lenta de red no debería mantener una transacción abierta); ponerlo en el dominio lo acopla a Spring, que es justo lo que se busca evitar.

---

## Aggregate Roots

Cada slice tiene uno o más *aggregate roots*: la entidad de dominio que es punto de entrada obligatorio para leer o modificar un grupo de objetos relacionados, y la única responsable de mantener sus invariantes.

- **`content/`**: `Temario` es aggregate root de su árbol (`listaAResponder`, que puede contener `Pregunta`s o sub-`Temario`s). `Pregunta` (y sus subclases) es aggregate root de sí misma cuando se la edita o consulta de forma independiente (fuera del árbol de un `Temario`, p. ej. al editar una pregunta puntual).
- **`answering/`**: `Respuesta` es aggregate root de un intento de respuesta y de la transición de `EstadoCritico` que ese intento dispara.

Regla práctica: **solo el aggregate root se persiste/recupera a través de un repository port.** Sus objetos internos (p. ej. una `Opcion` dentro de `SeleccionUnica`) nunca tienen su propio repository — se cargan y guardan siempre como parte del root.

---

## Validaciones por capa

```
Controller (infra)
    │  validaciones sintácticas — @NotNull, @Size, @NotBlank
    ▼
Handler (application)
    │  orquesta, no valida reglas de negocio
    ▼
Domain
    │  validaciones de negocio — invariantes, reglas del dominio
```

Ejemplo concreto: que el campo `enunciado` no sea nulo es una validación **sintáctica** (`@NotBlank` en el DTO del Controller). Que "un cuestionario no puede tener dos preguntas con el mismo enunciado" es una validación de **negocio** — vive en `domain/` (p. ej. en `Temario.agregarALaLista`), lanzando la excepción de dominio correspondiente (`BussinesException`), no una `IllegalArgumentException` de infraestructura.

---

## Mappers

`FabricaDePreguntas` usa hoy `BeanUtils.copyProperties`, que acopla el dominio a Spring y falla en silencio ante renombres de campos. Se elimina por completo del dominio.

En su lugar:
- **MapStruct** cuando el mapeo es mecánico campo-a-campo (la mayoría de `Entity ↔ Domain`).
- **Mappers manuales** cuando el dominio tiene lógica de conversión o invariantes que validar (p. ej. construir la jerarquía de `Pregunta` según `TipoAResponder`).

Los mappers viven siempre en `infrastructure/persistence/mapper/`, **nunca en `domain/`**. El dominio no sabe que existe un mapper ni una Entity — el flujo de mapeo siempre va en una sola dirección posible:

```
Controller → Handler → Mapper → Domain
```

Nunca al revés. Si `domain/` necesitara construir una Entity o un DTO de infraestructura (`new PreguntaEntity(...)` desde dentro del dominio), es una señal de que el mapeo se está haciendo en el lugar equivocado.

---

## `shared/` — regla de admisión

`shared/` tiende a convertirse en el basurero común de un proyecto (`DateUtils`, `StringUtils`, `MapperUtils`, `RandomUtils`...) hasta que todo el proyecto termina dependiendo de él y ya no protege nada.

**Regla:** `shared/` solo contiene conceptos verdaderamente universales que no pertenecen a ningún dominio específico — tipos base sin lógica de negocio (`EntityId`, `DomainEvent`), la excepción de dominio genérica (`BussinesException`), y utilidades sin ninguna nota del dominio de preguntas/cuestionarios. Antes de agregar algo a `shared/`, preguntarse: *¿esto tendría sentido en un proyecto completamente distinto?* Si la respuesta depende de "preguntas", "cuestionarios" o "críticos", no va en `shared/` — va en el slice que lo necesita, y si dos slices lo necesitan, se duplica antes que promoverlo prematuramente a `shared/`.

---

## Por qué `Temario` y `Pregunta` están en el mismo slice

Separarlos sería cortar por *tipo de entidad*, que es lo mismo que hace una arquitectura en capas tradicional pero en vertical — no cambia nada conceptualmente.

El vertical slicing corta por **comportamiento de negocio**:
- Un usuario no "gestiona temarios" por un lado y "gestiona preguntas" por otro.
- Los gestiona juntos dentro del mismo flujo de autoría.
- Por eso ambos viven en `content/`.

Si en el futuro aparece "banco de preguntas reutilizables entre cuestionarios", ahí sí justifica su propio slice — porque es un *comportamiento nuevo*, no porque las preguntas sean un tipo distinto de entidad.

---

## Domain Model vs Persistence Model

### El problema actual

Los objetos de dominio (`AResponder`, `Temario`, `Pregunta` y subclases) tienen anotaciones de frameworks mezcladas con lógica de negocio:

- `@Entity`, `@Table`, `@Inheritance`, `@PostLoad` — acoplan el dominio a JPA
- `@JsonView` — acopla el dominio a Jackson/HTTP
- `BeanUtils.copyProperties` en `FabricaDePreguntas` — acopla el dominio a Spring

**Consecuencia:** no se puede testear el dominio sin levantar Spring/JPA.

### La solución: dos versiones de cada entidad

**Objeto de dominio** — Java puro, contiene toda la lógica:
```java
// content/domain/Temario.java
public class Temario {
    private Long id;
    private String nombre;
    private List<AResponder> listaAResponder;

    public void agregarALaLista(AResponder item) { ... }
    public boolean contieneCritico() { ... }
}
```

**Entidad JPA** — sin lógica, solo para persistir:
```java
// content/infrastructure/persistence/entity/TemarioEntity.java
@Entity
@Table(name = "temario")
@Inheritance(strategy = InheritanceType.JOINED)
public class TemarioEntity {
    @Id @GeneratedValue
    private Long id;
    private String nombre;

    @OneToMany
    private List<AResponderEntity> listaAResponder;
}
```

**Mapper entre los dos:**
```java
// content/infrastructure/persistence/mapper/TemarioMapper.java
public class TemarioMapper {
    public Temario toDomain(TemarioEntity entity) { ... }
    public TemarioEntity toEntity(Temario domain) { ... }
}
```

### El costo

Por cada entidad de dominio se necesitan: objeto de dominio (limpiar anotaciones) + entity JPA (nueva) + mapper (nuevo).
Con 8 subclases de `Pregunta` más `AResponder` y `Temario` son aproximadamente **20 clases nuevas** de infraestructura. Trabajo mecánico pero predecible.

### Beneficios

- Tests de dominio sin Spring — `new Temario()` funciona en cualquier test unitario
- Libertad de cambiar el esquema de BD sin tocar el dominio
- Libertad de cambiar el ORM sin tocar el dominio
- Lógica de negocio concentrada en `domain/`, sin ruido de anotaciones

---

## Consideraciones para la migración

El obstáculo principal es que actualmente el dominio está acoplado a JPA (`@Entity`, `@PostLoad`, `InheritanceType.JOINED`). La migración más segura es incremental:

0. **Cubrir con tests los casos de uso críticos antes de tocar nada.** Una migración arquitectónica sin tests de caracterización previos suele introducir regresiones silenciosas — el objetivo no es solo que compile, es que el comportamiento observable no cambie.
1. Crear las entidades JPA nuevas (`XxxEntity`) sin borrar las originales todavía.
2. Crear los mappers (manuales o MapStruct), sin `BeanUtils`.
3. Crear los adapters JPA que usan las nuevas entities.
4. Definir los puertos de salida (`port/out`) y los `command`/`query` + `Handler` como clases separadas.
5. Definir la `api/` pública del slice (solo lo que otros slices necesitan consumir).
6. Migrar los consumidores entre slices para que dependan de `api/` en vez de acceder a la BD del otro slice directamente.
7. Migrar un caso de uso a la vez para validar que funciona.
8. Limpiar las anotaciones de los objetos de dominio originales.
9. Borrar código que quedó sin usar.

Hacer el cambio incremental mantiene el sistema funcionando durante la migración.

---

## Deuda de nomenclatura pendiente

**Estado real a la fecha de este documento:** `content/application/` fue construido con el patrón `port/in/XxxUseCase` (interfaz) + `application/service/XxxService` (implementación), no con `Command`/`Query` + `Handler` como describe este documento. `answering/application/command/` sí sigue el patrón `Command` + `Handler`.

**Decisión:** el código existente de `content/` **queda tal cual está**. No se renombra retroactivamente solo para cumplir este documento. El patrón `Command/Handler` descrito acá es el estándar para:
- Código nuevo en cualquier slice, desde ahora.
- El día que se decida migrar `content/` a este patrón (sin fecha definida — es deuda técnica documentada, no una tarea en curso).

Hasta entonces, al leer `content/`, `UseCase` (interfaz en `port/in/`) equivale conceptualmente a `Command`/`Query` de este documento, y `Service` equivale a `Handler`.

---

## Cuándo agregar un slice nuevo

Solo cuando aparezca una capacidad de negocio diferenciada que:
- Tiene su propio ciclo de vida
- Puede cambiar independientemente de los demás
- No comparte responsabilidad con los slices existentes

Ejemplos futuros posibles: `import-export/`, `statistics/`, `shared-question-bank/`.
