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
│   ├── domain/
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
│   │   │   ├── in/                              ← interfaces de entrada (qué puede hacer el usuario)
│   │   │   │   ├── CrearCuestionarioUseCase.java
│   │   │   │   ├── CrearPreguntaUseCase.java
│   │   │   │   ├── EditarTemarioUseCase.java
│   │   │   │   ├── EliminarTemarioUseCase.java
│   │   │   │   ├── EliminarPreguntaUseCase.java
│   │   │   │   └── ObtenerCuestionariosUseCase.java
│   │   │   └── out/                             ← interfaces de salida (qué necesita el dominio)
│   │   │       ├── TemarioRepositoryPort.java
│   │   │       └── PreguntaRepositoryPort.java
│   │   └── service/                             ← implementaciones de los use cases
│   │       ├── CrearCuestionarioService.java
│   │       ├── CrearPreguntaService.java
│   │       └── EditarTemarioService.java
│   └── infrastructure/
│       ├── persistence/                         ← adaptadores OUT: implementan los repository ports
│       │   ├── TemarioJpaAdapter.java
│       │   └── PreguntaJpaAdapter.java
│       └── controller/                          ← adaptadores IN: reciben HTTP, llaman use cases
│           ├── TemarioController.java
│           └── PreguntaController.java
│
└── answering/
    ├── domain/
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

## Consideraciones para la migración

El obstáculo principal es que actualmente el dominio está acoplado a JPA (`@Entity`, `@PostLoad`, `InheritanceType.JOINED`). La migración más segura es incremental:

1. Definir los puertos (interfaces) sin mover nada todavía.
2. Crear los use cases como clases separadas delegando en los servicios actuales.
3. Separar entidades JPA de objetos de dominio empezando por las subclases más simples.
4. Limpiar anotaciones de framework del dominio al final.

Hacer el cambio incremental mantiene el sistema funcionando durante la migración.

---

## Cuándo agregar un slice nuevo

Solo cuando aparezca una capacidad de negocio diferenciada que:
- Tiene su propio ciclo de vida
- Puede cambiar independientemente de los demás
- No comparte responsabilidad con los slices existentes

Ejemplos futuros posibles: `import-export/`, `statistics/`, `shared-question-bank/`.
