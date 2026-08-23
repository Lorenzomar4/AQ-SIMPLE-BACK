# ¿Por qué existe la tabla `temario` si solo tiene una columna `id`?

## Resumen

`temario` es la tabla "hoja" de la rama contenedora en la jerarquía de herencia
`JOINED` de `AResponder`. A simple vista parece inútil porque no aporta ninguna
columna propia — pero su existencia **no es sobre almacenar datos, es sobre
integridad referencial**: le permite a la base de datos garantizar que solo un
contenedor (`CUESTIONARIO`, `TEMA` o `SUBTEMA`) puede ser padre de otro nodo,
nunca una pregunta.

## Contexto: la estrategia `InheritanceType.JOINED`

`AResponderEntity` (`src/main/java/.../persistence/entity/AResponderEntity.java`)
define la tabla base `aresponder` con los campos comunes a todo lo que puede
"responderse": `id`, `titulo`, `descripcion`, `tipo`, `id_del_duenio`,
`fecha_de_creacion`, `ultima_actualizacion`.

De ahí cuelgan dos ramas, cada una con su propia sub-jerarquía `JOINED`:

- **`PreguntaEntity`** (`.../persistence/entity/PreguntaEntity.java`) — añade
  columnas propias (`intentos_para_que_deje_de_ser_critico_disponible`,
  `imagen_titulo`) y luego se especializa en `pregunta_simple`,
  `verdaderoofalso`, `seleccion_unica`, `opcion_multiple`,
  `desplegable_compartido`, `desplegable_independiente` — cada una con sus
  propias columnas.
- **`TemarioEntity`** (`.../persistence/entity/TemarioEntity.java`) — **no
  añade ninguna columna**, es literalmente:

  ```java
  @Entity
  @Table(name = "temario")
  public class TemarioEntity extends AResponderEntity {
  }
  ```

Con `JOINED`, cada subclase concreta obtiene su propia tabla enlazada por
`id` a la tabla padre. Como `TemarioEntity` no declara campos nuevos, su
tabla queda reducida a una columna: `id bigint primary key references
aresponder(id)`.

## ¿Por qué `Temario` no necesita columnas propias?

Porque `CUESTIONARIO`, `TEMA` y `SUBTEMA` son estructuralmente idénticos —
todos son "un nodo que agrupa otros nodos" — y se diferencian entre sí por el
discriminador `tipo` (columna `aresponder.tipo`, con el `CHECK` que ya viste),
no por columnas distintas. La lógica de qué tipo le corresponde a un hijo
nuevo la decide en tiempo de ejecución el patrón Strategy `TipoDeTemario`
(`TipoCuestionario` vs `TipoTema`), no el esquema.

Eso es distinto al caso de `Pregunta`: cada tipo de pregunta sí tiene forma
de datos distinta (`pregunta_simple` guarda `respuesta_establecida`,
`verdaderoofalso` guarda `respuesta_verdadera`, etc.), así que ahí `JOINED`
sí paga su precio con columnas reales por subtipo.

## La razón real de ser: integridad referencial en `id_del_duenio`

La columna `aresponder.id_del_duenio` es la FK que apunta al padre de
cualquier nodo (pregunta o temario). En el DDL:

```sql
alter table aresponder
    add constraint fkfhr1in1fs32khyjpolrnvke7a
        foreign key (id_del_duenio) references temario;
```

Fíjate que la FK apunta a **`temario(id)`, no a `aresponder(id)`**. Esa es la
clave: al apuntar a la tabla hoja de la rama contenedora en vez de a la tabla
base, Postgres impone en el esquema una regla de negocio que de otra forma
tendrías que validar a mano en la aplicación:

> **Solo un id que exista en `temario` puede ser el dueño (`id_del_duenio`)
> de otro nodo.**

Es decir, una `Pregunta` nunca puede ser padre de nada — la base de datos lo
rechaza con una violación de FK antes de que el bug llegue a producción. Si
`id_del_duenio` apuntara a `aresponder(id)` en su lugar, cualquier `id` de
cualquier pregunta sería un valor válido para "padre", y solo el código Java
evitaría (o no) que alguien anide una pregunta dentro de otra.

## Qué pasaría si se eliminara la tabla `temario`

Si se colapsara todo en una sola tabla `aresponder` (herencia
`SINGLE_TABLE`) o si `temario` se eliminara y la FK apuntara a `aresponder`:

- Se perdería la garantía a nivel de esquema de que solo contenedores pueden
  tener hijos.
- Habría que reimplementar esa validación en cada `Service`/`UseCase` que
  cree o edite un nodo — con el riesgo de que algún camino nuevo se olvide de
  validarlo.
- Un `INSERT`/`UPDATE` directo en la base (migración manual, script de datos,
  fix de soporte) podría crear estados inválidos sin que nada lo impida.

## Conclusión

`temario` es un ejemplo legítimo del patrón "tabla hoja vacía" que aparece en
herencia `JOINED` cuando una rama de la jerarquía no necesita atributos
propios. Su columna `id` no está ahí para guardar datos — está ahí para ser
el destino de una foreign key que expresa una regla de dominio
("solo los contenedores pueden ser padres") directamente en el esquema, en
vez de dejarla como una convención que solo vive en el código Java.

## Referencias en el código

- `src/main/java/com/lorenzomar3/AQ/content/infrastructure/persistence/entity/AResponderEntity.java`
- `src/main/java/com/lorenzomar3/AQ/content/infrastructure/persistence/entity/TemarioEntity.java`
- `src/main/java/com/lorenzomar3/AQ/content/infrastructure/persistence/entity/PreguntaEntity.java`
- `src/main/java/com/lorenzomar3/AQ/content/domain/Temario.java` (estrategia `TipoDeTemario`)
