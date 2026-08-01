package com.lorenzomar3.AQ.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Este test verifica, a través del controller real (MockMvc), que los 4 tipos de pregunta
 * no migrados por este spec (SELECCION_UNICA, OPCION_MULTIPLE, DESPLEGABLE_COMPARTIDO,
 * DESPLEGABLE_INDEPENDIENTE) siguen respondiendo el mismo contenido que antes, ya que el
 * nuevo dispatch por tipo en PreguntaController cae para ellos al mismo camino viejo
 * (preguntaService), sin cambios.
 *
 * Las comparaciones se hacen con content().json(...) en modo no-estricto (lenient): no
 * exige orden en las listas (las consultas JPA no tienen ORDER BY) y permite campos extra
 * en la respuesta real -en particular, los campos que revelan la respuesta correcta, cuyo
 * eventual leak en la vista fetch es un bug preexistente fuera de alcance de este spec y
 * que este test no corrige ni verifica.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FetchQuestionTiposNoMigradosNoRegresionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private CrearSeleccionUnicaUseCase crearSeleccionUnicaUseCase;

    @Autowired
    private CrearOpcionMultipleUseCase crearOpcionMultipleUseCase;

    @Autowired
    private CrearDesplegableCompartidoUseCase crearDesplegableCompartidoUseCase;

    @Autowired
    private CrearDesplegableIndependienteUseCase crearDesplegableIndependienteUseCase;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba no-regresion fetch", TipoAResponder.CUESTIONARIO);
        temarioId = temarioRepository.save(temario).getId();
    }

    @AfterEach
    void limpiar() {
        preguntasCreadas.forEach(id -> {
            try {
                preguntaRepositoryViejo.deleteById(id);
            } catch (Exception ignored) {
            }
        });
        temarioRepository.deleteById(temarioId);
    }

    private Long obtenerUltimoHijo() {
        Temario temario = temarioRepository.findById(temarioId).orElseThrow();
        return temario.getListaAResponder().stream().mapToLong(AResponder::getId).max().orElseThrow();
    }

    private void verificarFetchYFetchFullContienenElContenidoEsperado(Long id, TipoAResponder tipo, String jsonEsperadoParcial) throws Exception {
        ObtenerPreguntaDTO dto = new ObtenerPreguntaDTO(id, tipo);
        String body = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/questions/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonEsperadoParcial, false));

        mockMvc.perform(post("/questions/fetch-full")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonEsperadoParcial, false));
    }

    @Test
    void seleccionUnicaFetchYFetchFullSiguenIgualQueAntes() throws Exception {
        List<Opcion> opciones = List.of(new Opcion("River Plate", false), new Opcion("Boca Juniors", true));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta seleccion unica", "Descripcion", TipoAResponder.SELECCION_UNICA,
                temarioId, null, null, null, opciones, null, null);
        crearSeleccionUnicaUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        String jsonEsperado = """
                {
                  "id": %d,
                  "titulo": "Pregunta seleccion unica",
                  "descripcion": "Descripcion",
                  "idDuenio": %d,
                  "tipo": "SELECCION_UNICA",
                  "intentosParaQueDejeDeSerCriticoDisponible": 0,
                  "listaDeOpciones": [
                    {"opcion": "River Plate"},
                    {"opcion": "Boca Juniors"}
                  ]
                }
                """.formatted(id, temarioId);

        verificarFetchYFetchFullContienenElContenidoEsperado(id, TipoAResponder.SELECCION_UNICA, jsonEsperado);
    }

    @Test
    void opcionMultipleFetchYFetchFullSiguenIgualQueAntes() throws Exception {
        List<Opcion> opciones = List.of(new Opcion("River Plate", true), new Opcion("Boca Juniors", true), new Opcion("Independiente", false));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta opcion multiple", "Descripcion", TipoAResponder.OPCION_MULTIPLE,
                temarioId, null, null, null, opciones, null, null);
        crearOpcionMultipleUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        String jsonEsperado = """
                {
                  "id": %d,
                  "titulo": "Pregunta opcion multiple",
                  "descripcion": "Descripcion",
                  "idDuenio": %d,
                  "tipo": "OPCION_MULTIPLE",
                  "intentosParaQueDejeDeSerCriticoDisponible": 0,
                  "listaDeOpciones": [
                    {"opcion": "River Plate"},
                    {"opcion": "Boca Juniors"},
                    {"opcion": "Independiente"}
                  ]
                }
                """.formatted(id, temarioId);

        verificarFetchYFetchFullContienenElContenidoEsperado(id, TipoAResponder.OPCION_MULTIPLE, jsonEsperado);
    }

    @Test
    void desplegableCompartidoFetchYFetchFullSiguenIgualQueAntes() throws Exception {
        List<OpcionDeDesplegableCompartido> opciones = List.of(
                new OpcionDeDesplegableCompartido("River Plate es un club de", "futbol"),
                new OpcionDeDesplegableCompartido("Boca Juniors es un club de", "futbol"));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta desplegable compartido", "Descripcion", TipoAResponder.DESPLEGABLE_COMPARTIDO,
                temarioId, null, null, null, null, opciones, null);
        crearDesplegableCompartidoUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        String jsonEsperado = """
                {
                  "id": %d,
                  "titulo": "Pregunta desplegable compartido",
                  "descripcion": "Descripcion",
                  "idDuenio": %d,
                  "tipo": "DESPLEGABLE_COMPARTIDO",
                  "intentosParaQueDejeDeSerCriticoDisponible": 0,
                  "listaDeOpciones": [
                    {"pregunta": "River Plate es un club de"},
                    {"pregunta": "Boca Juniors es un club de"}
                  ]
                }
                """.formatted(id, temarioId);

        verificarFetchYFetchFullContienenElContenidoEsperado(id, TipoAResponder.DESPLEGABLE_COMPARTIDO, jsonEsperado);
    }

    @Test
    void desplegableIndependienteFetchYFetchFullSiguenIgualQueAntes() throws Exception {
        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntas = List.of(
                new SeleccionUnicaParaDesplegableIndependiente("Los mamiferos son", List.of(
                        new Opcion("Vertebrados", true),
                        new Opcion("Invertebrados", false))),
                new SeleccionUnicaParaDesplegableIndependiente("Los peces son", List.of(
                        new Opcion("Acuaticos", true),
                        new Opcion("Terrestres", false))));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta desplegable independiente", "Descripcion", TipoAResponder.DESPLEGABLE_INDEPENDIENTE,
                temarioId, null, null, null, null, null, subPreguntas);
        crearDesplegableIndependienteUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        String jsonEsperado = """
                {
                  "id": %d,
                  "titulo": "Pregunta desplegable independiente",
                  "descripcion": "Descripcion",
                  "idDuenio": %d,
                  "tipo": "DESPLEGABLE_INDEPENDIENTE",
                  "intentosParaQueDejeDeSerCriticoDisponible": 0,
                  "listaDeOpciones": [
                    {"titulo": "Los mamiferos son", "listaDeOpcionesDisponible": [{"opcion": "Vertebrados"}, {"opcion": "Invertebrados"}]},
                    {"titulo": "Los peces son", "listaDeOpcionesDisponible": [{"opcion": "Acuaticos"}, {"opcion": "Terrestres"}]}
                  ]
                }
                """.formatted(id, temarioId);

        verificarFetchYFetchFullContienenElContenidoEsperado(id, TipoAResponder.DESPLEGABLE_INDEPENDIENTE, jsonEsperado);
    }
}
