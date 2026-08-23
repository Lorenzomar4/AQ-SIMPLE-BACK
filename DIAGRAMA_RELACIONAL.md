# Diagrama Relacional — Esquema de Base de Datos

DER en ASCII a partir de los scripts DDL de PostgreSQL. Refleja la jerarquía `InheritanceType.JOINED` de JPA sobre `AResponder` descrita en `CLAUDE.md`: cada subtipo tiene su propia tabla, unida por `id` a su tabla padre.

> El bloque es ancho — abrilo en un editor de texto plano o con "wrap" desactivado para verlo alineado correctamente (no usa tabs, solo espacios).

```
══════════════════════════════════════════════════════════════════════════
  1) JERARQUIA DE HERENCIA (InheritanceType.JOINED sobre AResponder)
     la caja hija comparte el mismo id que la caja padre (1:1)
══════════════════════════════════════════════════════════════════════════

                                                                                                                      ┌───────────────────────────────────────────────────────────┐
                                                                                                                      │                        ARESPONDER                         │
                                                                                                                      ├───────────────────────────────────────────────────────────┤
                                                                                                                      │ PK  id                       : bigserial                  │
                                                                                                                      │     descripcion              : oid                        │
                                                                                                                      │     fecha_de_creacion        : timestamp(6)               │
                                                                                                                      │ FK  id_del_duenio ─► TEMARIO.id                           │
                                                                                                                      │     tipo                     : varchar(255)  [CHECK enum] │
                                                                                                                      │     titulo                   : varchar(10500)             │
                                                                                                                      │     ultima_actualizacion     : timestamp(6)               │
                                                                                                                      └───────────────────────────────────────────────────────────┘
               ┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┴───────────────┐
┌────────────────────────────┐                                                                                                    ┌───────────────────────────────────────────────────────────────────┐
│          TEMARIO           │                                                                                                    │                             PREGUNTA                              │
├────────────────────────────┤                                                                                                    ├───────────────────────────────────────────────────────────────────┤
│ PK/FK  id ─► ARESPONDER.id │                                                                                                    │ PK/FK  id ─► ARESPONDER.id                                        │
└────────────────────────────┘                                                                                                    │        imagen_titulo       : varchar(1500)                        │
                                                                                                                                  │        intentos_para_que_deje_de_ser_critico_disponible : integer │
                                                                                                                                  └───────────────────────────────────────────────────────────────────┘
                                                     ┌───────────────────────────────────────────┬────────────────────────────────────┬─────────────────────────────┼───────────────────────────────────────┬──────────────────────────────────────────────────────────┐
                                ┌────────────────────────────────────────┐  ┌────────────────────────────────────────┐  ┌──────────────────────────┐  ┌──────────────────────────┐            ┌──────────────────────────┐                               ┌───────────────────────────┐
                                │            PREGUNTA_SIMPLE             │  │            VERDADEROOFALSO             │  │     SELECCION_UNICA      │  │     OPCION_MULTIPLE      │            │  DESPLEGABLE_COMPARTIDO  │                               │ DESPLEGABLE_INDEPENDIENTE │
                                ├────────────────────────────────────────┤  ├────────────────────────────────────────┤  ├──────────────────────────┤  ├──────────────────────────┤            ├──────────────────────────┤                               ├───────────────────────────┤
                                │ PK/FK  id ─► PREGUNTA.id               │  │ PK/FK  id ─► PREGUNTA.id               │  │ PK/FK  id ─► PREGUNTA.id │  │ PK/FK  id ─► PREGUNTA.id │            │ PK/FK  id ─► PREGUNTA.id │                               │ PK/FK  id ─► PREGUNTA.id  │
                                │        respuesta_establecida : text    │  │        respuesta_verdadera   : boolean │  └──────────────────────────┘  └──────────────────────────┘            └──────────────────────────┘                               └───────────────────────────┘
                                │        respuesta_precisa     : boolean │  └────────────────────────────────────────┘                                                                                      │                                                          │
                                └────────────────────────────────────────┘                                                                                                          ┌──────────────────────────────────────────────┐  ┌─────────────────────────────────────────────────────────────────┐
                                                                                                                                                                                    │       OPCION_DE_DESPLEGABLE_COMPARTIDO       │  │         SELECCION_UNICA_PARA_DESPLEGABLE_INDEPENDIENTE          │
                                                                                                                                                                                    ├──────────────────────────────────────────────┤  ├─────────────────────────────────────────────────────────────────┤
                                                                                                                                                                                    │ PK  id            : bigserial                │  │ PK  id                          : bigserial                     │
                                                                                                                                                                                    │     pregunta      : varchar(255)             │  │     titulo                      : varchar(255)                  │
                                                                                                                                                                                    │     respuesta     : varchar(255)             │  │ FK  id_pregunta_desplegable_ind ─► DESPLEGABLE_INDEPENDIENTE.id │
                                                                                                                                                                                    │ FK  id_pregunta ─► DESPLEGABLE_COMPARTIDO.id │  └─────────────────────────────────────────────────────────────────┘
                                                                                                                                                                                    └──────────────────────────────────────────────┘

══════════════════════════════════════════════════════════════════════════
  2) TABLAS ASOCIATIVAS (relación N:1 normal, no herencia)
     el campo FK indica dentro de la caja a qué tabla/columna apunta
══════════════════════════════════════════════════════════════════════════

┌───────────────────────────────────┐      ┌───────────────────────────────────────────────────────────────────────────────────────────────┐
│       TEORIA_DE_LA_PREGUNTA       │      │                                            OPCION                                             │
├───────────────────────────────────┤      ├───────────────────────────────────────────────────────────────────────────────────────────────┤
│ PK  id           : bigserial      │      │ PK  id                            : bigserial                                                 │
│     imagen       : varchar(1000)  │      │     la_respuesta_es               : boolean                                                   │
│     respuesta    : varchar(10500) │      │     opcion                        : varchar(255)                                              │
│ FK  id_pregunta ─► PREGUNTA.id    │      │ FK  id_seleccion_unica            ─► SELECCION_UNICA.id             (null)                    │
└───────────────────────────────────┘      │ FK  id_multiple_opcion            ─► OPCION_MULTIPLE.id             (null)                    │
                                           │ FK  id_desplegable_independiente  ─► SELECCION_UNICA_PARA_DESPLEGABLE_INDEPENDIENTE.id (null) │
                                           └───────────────────────────────────────────────────────────────────────────────────────────────┘
```

## Cómo leerlo

- **Sección 1 (árbol con líneas):** es la columna vertebral de la herencia `JOINED`. Cada caja hija comparte el mismo `id` que su padre (`PK/FK id ─► TABLA_PADRE.id`) — es la misma fila lógica repartida en varias tablas. Las líneas `┌┬┴┼─│` muestran exactamente qué tabla extiende a cuál, tal como está mapeado en `model/AResponder/`.
- **Sección 2 (cajas sueltas):** son tablas de asociación normales (N:1), no herencia. En vez de dibujar líneas cruzando todo el diagrama (ilegible con 3 FKs saliendo de `OPCION` hacia ramas distintas), cada fila `FK` indica directamente con `─►` a qué tabla y columna apunta.
- `PK` = clave primaria, `FK` = clave foránea, `PK/FK` = la clave primaria es a la vez foránea hacia la tabla padre (patrón de herencia `JOINED`).

## Notas del modelo

- **`aresponder.tipo`** es el discriminador (`TipoAResponder`), restringido por `CHECK` a 9 valores: `CUESTIONARIO`, `TEMA`, `SUBTEMA` y los 6 tipos de pregunta.
- **`aresponder.id_del_duenio → temario.id`** es la relación de contención: un `Temario` (cuestionario/tema/subtema) agrupa una lista de `AResponder` hijos (preguntas u otros temarios).
- **`OPCION`** es compartida por tres tipos de pregunta vía tres FKs *nullable* mutuamente excluyentes (`id_seleccion_unica`, `id_multiple_opcion`, `id_desplegable_independiente`).
- **`SELECCION_UNICA_PARA_DESPLEGABLE_INDEPENDIENTE`** es tabla intermedia propia de `DESPLEGABLE_INDEPENDIENTE`, y sus filas a su vez son referenciadas por `OPCION`.
- **`TEORIA_DE_LA_PREGUNTA`** cuelga 1:N de cualquier `PREGUNTA` (se serializa solo en la vista `Full` de Jackson, según `model/View.java`).
