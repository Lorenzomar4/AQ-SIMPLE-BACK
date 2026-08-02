# Arquitectura objetivo — AQ-SIMPLE-BACK

## Patrones aplicados

- **Hexagonal (Ports & Adapters)** — el dominio no depende de ningún framework. Spring, JPA y Jackson viven en la capa de infraestructura.
- **Vertical Slicing** — el código se organiza por capacidad de negocio, no por tipo técnico. Cada slice es autocontenido.
- **CQRS ligero dentro de cada slice** — `application/` separa `command/` (escritura) de `query/` (lectura), sin necesidad de almacenes o modelos de datos distintos.

---

## Principios del proyecto

1. **Alta cohesión** — cada slice representa una capacidad de negocio completa (autoría, respuesta, ...), no una capa técnica.
2. **Bajo acoplamiento** — los slices solo colaboran mediante contratos públicos (`api/`), nunca mediante sus clases internas.
3. **Dominio puro** — el dominio no conoce frameworks: sin `@Entity`, sin `@JsonView`, sin `BeanUtils`.
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

~~`answering` nunca importa clases de `content`; accede a la BD directamente~~ — esta regla original solo cambiaba *dónde* ocurría el acoplamiento (de "importar una clase" a "conocer el esquema de la tabla de otro slice"), no lo eliminaba.

La regla correcta:

> **Los slices no dependen de la implementación interna de otros slices.** Si necesitan colaborar, lo hacen exclusivamente a través de la **API pública** de ese slice: interfaces expuestas en su paquete `api/`, DTOs de solo lectura, o eventos de dominio. Nunca mediante entidades, repositorios, mappers o adapters de otro slice.

Concretamente, `answering` no conoce `PreguntaEntity`, `PreguntaRepository`, `PreguntaMapper` ni ningún `JpaAdapter` de `content`. Solo conoce lo que `content.api` decide exponer (p. ej. `PreguntaView`, `ObtenerPreguntaQuery`).

---

## Visibilidad por paquete

Dentro de cada slice, solo el paquete `api/` es público (`public`). El resto de las clases (`domain`, `application`, `infrastructure`) deberían ser *package-private* siempre que Java lo permita (esto es limitado entre subpaquetes, pero es la intención a respetar incluso cuando el compilador no lo puede forzar del todo). Así el propio diseño de paquetes ayuda a mantener el límite del slice, y no depende solo de la disciplina del equipo.

---

## API pública de un slice

Cada slice expone su capacidad hacia otros slices a través de un paquete `api/` — nunca a través de `application/` o `infrastructure/` directamente:

```java
// content/api/PreguntaView.java — DTO de solo lectura, estable, sin anotaciones JPA
public record PreguntaView(Long id, String enunciado, TipoAResponder tipo) { }

// content/api/ObtenerPreguntaQuery.java — contrato público de consulta
public interface ObtenerPreguntaQuery {
    PreguntaView obtenerPorId(Long id);
}
```

La implementación real (`content/application/query/ObtenerPreguntaHandler.java`) vive en `application/` y se expone a través de esa interfaz — pero quien la consume desde otro slice (`answering`) solo importa `content.api.*`.

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
│   │   ├── ObtenerPreguntaQuery.java
│   │   └── ObtenerPreguntaService.java
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
│   │   │   ├── ObtenerCuestionariosHandler.java
│   │   │   └── ObtenerPreguntaHandler.java           ← implementa content.api.ObtenerPreguntaQuery
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
    │   │   └── ObtenerCriticosHandler.java
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

## Flujo de dependencias

### Dentro de cada slice

```
Controller (infra)
    │ llama
    ▼
Command/Query (application)
    │ ejecutado por
    ▼
XxxHandler (application/command | application/query)
    │ llama
    ▼
RepositoryPort port/out (application)
    │ implementado por
    ▼
JpaAdapter (infra/persistence)
    │ usa
    ▼
Base de datos
```

### Entre slices

```
answering/application/command/ResponderPreguntaHandler
    depende de ──→ content/api/ObtenerPreguntaQuery   (interfaz pública, DTO PreguntaView)
                    nunca de content/domain, content/application ni content/infrastructure

content/application/query/ObtenerPreguntaHandler
    implementa ──→ content/api/ObtenerPreguntaQuery
                    (traduce su modelo interno a PreguntaView antes de responder)
```

`answering` deja de tener su propio puerto `out` para "leer preguntas" (el antiguo `PreguntaParaResponderPort` accediendo a BD): esa responsabilidad ahora es de `content`, que la publica en `content.api`. `answering` solo conserva puertos `out` sobre **su propio** dato (`RespuestaRepositoryPort`).

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

---

## Reglas de dependencia (las más importantes)

| Capa / concepto | Puede depender de | No puede depender de |
|------|-------------------|----------------------|
| `domain/` | Nada (Java puro), `shared/domain/` | application, infrastructure, frameworks |
| `application/` | `domain/`, `shared/` | `infrastructure/`, Spring, JPA |
| `infrastructure/` | `application/`, `domain/` | Otros slices |
| Un slice completo | La **`api/`** de otro slice, `shared/` | `domain/`, `application/` (fuera de `api/`) o `infrastructure/` de otro slice |

---

## Mappers

`FabricaDePreguntas` usa hoy `BeanUtils.copyProperties`, que acopla el dominio a Spring y falla en silencio ante renombres de campos. Se elimina por completo del dominio.

En su lugar:
- **MapStruct** cuando el mapeo es mecánico campo-a-campo (la mayoría de `Entity ↔ Domain`).
- **Mappers manuales** cuando el dominio tiene lógica de conversión o invariantes que validar (p. ej. construir la jerarquía de `Pregunta` según `TipoAResponder`).

Los mappers viven siempre en `infrastructure/persistence/mapper/`, nunca en `domain/`.

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

## Cuándo agregar un slice nuevo

Solo cuando aparezca una capacidad de negocio diferenciada que:
- Tiene su propio ciclo de vida
- Puede cambiar independientemente de los demás
- No comparte responsabilidad con los slices existentes

Ejemplos futuros posibles: `import-export/`, `statistics/`, `shared-question-bank/`.
