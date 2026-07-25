# Arquitectura objetivo — AQ-SIMPLE-BACK

## Patrones aplicados

- **Hexagonal (Ports & Adapters)** — el dominio no depende de ningún framework. Spring, JPA y Jackson viven en la capa de infraestructura.
- **Vertical Slicing** — el código se organiza por capacidad de negocio, no por tipo técnico. Cada slice es autocontenido.

---

## Los dos slices

### `content/` — Autoría
Todo lo relacionado a crear, editar y estructurar el contenido del cuestionario.
Contiene: cuestionarios, temas, subtemas, preguntas de cualquier tipo.

### `answering/` — Respuesta
Todo lo relacionado al flujo de responder: obtener preguntas, verificar respuestas, gestionar críticos.

**Regla de dependencia entre slices:**
`answering` nunca importa clases de `content`. Define lo que necesita como puerto (`PreguntaParaResponderPort`) y la infraestructura lo satisface accediendo a la BD directamente.

---

## Estructura de paquetes

```
com/aq/
│
├── content/
│   ├── domain/                                      ← Java puro, sin anotaciones de frameworks
│   │   ├── AResponder.java
│   │   ├── Temario.java
│   │   ├── Pregunta.java
│   │   ├── PreguntaSimple.java
│   │   ├── VerdaderoOFalso.java
│   │   ├── SeleccionUnica.java
│   │   ├── OpcionMultiple.java
│   │   ├── DesplegableCompartido.java
│   │   ├── DesplegableIndependiente.java
│   │   └── FabricaDePreguntas.java
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/                                  ← interfaces de entrada (qué puede hacer el usuario)
│   │   │   │   ├── CrearCuestionarioUseCase.java
│   │   │   │   ├── CrearPreguntaUseCase.java
│   │   │   │   ├── EditarTemarioUseCase.java
│   │   │   │   ├── EliminarTemarioUseCase.java
│   │   │   │   ├── EliminarPreguntaUseCase.java
│   │   │   │   └── ObtenerCuestionariosUseCase.java
│   │   │   └── out/                                 ← interfaces de salida (qué necesita el dominio)
│   │   │       ├── TemarioRepositoryPort.java
│   │   │       └── PreguntaRepositoryPort.java
│   │   └── service/                                 ← implementaciones de los use cases
│   │       ├── CrearCuestionarioService.java
│   │       ├── CrearPreguntaService.java
│   │       └── EditarTemarioService.java
│   └── infrastructure/
│       ├── persistence/
│       │   ├── entity/                              ← entidades JPA (@Entity, sin lógica)
│       │   │   ├── AResponderEntity.java
│       │   │   ├── TemarioEntity.java
│       │   │   ├── PreguntaEntity.java
│       │   │   ├── PreguntaSimpleEntity.java
│       │   │   ├── VerdaderoOFalsoEntity.java
│       │   │   └── ... (resto de subclases)
│       │   ├── mapper/                              ← convierten Entity ↔ Domain
│       │   │   ├── TemarioMapper.java
│       │   │   └── PreguntaMapper.java
│       │   └── adapter/                             ← implementan los repository ports
│       │       ├── TemarioJpaAdapter.java
│       │       └── PreguntaJpaAdapter.java
│       └── controller/                              ← adaptadores IN: reciben HTTP, llaman use cases
│           ├── TemarioController.java
│           └── PreguntaController.java
│
└── answering/
    ├── domain/                                      ← Java puro, sin anotaciones de frameworks
    │   ├── Respuesta.java
    │   └── EstadoCritico.java
    ├── application/
    │   ├── port/
    │   │   ├── in/
    │   │   │   ├── ResponderPreguntaUseCase.java
    │   │   │   └── ObtenerCriticosUseCase.java
    │   │   └── out/
    │   │       └── PreguntaParaResponderPort.java   ← answering define lo que necesita
    │   └── service/
    │       ├── ResponderPreguntaService.java
    │       └── ObtenerCriticosService.java
    └── infrastructure/
        ├── persistence/
        │   └── PreguntaParaResponderAdapter.java    ← implementa el port, accede a JPA directamente
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
UseCase port/in (application)
    │ implementado por
    ▼
XxxService (application/service)
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
answering/infrastructure/PreguntaParaResponderAdapter
    implementa ──→ answering/application/port/out/PreguntaParaResponderPort
    accede a BD directamente sin importar nada de content/
```

---

## Reglas de dependencia (las más importantes)

| Capa | Puede depender de | No puede depender de |
|------|-------------------|----------------------|
| `domain/` | Nada (Java puro) | application, infrastructure, frameworks |
| `application/` | `domain/` | `infrastructure/`, Spring, JPA |
| `infrastructure/` | `application/`, `domain/` | Otros slices (solo via ports) |

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
2. Crear los mappers.
3. Crear los adapters JPA que usan las nuevas entities.
4. Definir los puertos (interfaces) y crear los use cases como clases separadas.
5. Migrar un use case a la vez para validar que funciona.
6. Limpiar las anotaciones de los objetos de dominio originales.
7. Borrar código que quedó sin usar.

Hacer el cambio incremental mantiene el sistema funcionando durante la migración.

---

## Cuándo agregar un slice nuevo

Solo cuando aparezca una capacidad de negocio diferenciada que:
- Tiene su propio ciclo de vida
- Puede cambiar independientemente de los demás
- No comparte responsabilidad con los slices existentes

Ejemplos futuros posibles: `import-export/`, `statistics/`, `shared-question-bank/`.
