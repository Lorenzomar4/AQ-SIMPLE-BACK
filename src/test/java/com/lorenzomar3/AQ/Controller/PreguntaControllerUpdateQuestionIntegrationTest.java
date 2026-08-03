package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PreguntaControllerUpdateQuestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemarioRepositoryPort temarioRepositoryPort;

    @Autowired
    private PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    @Autowired
    private VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    @Autowired
    private SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    @Autowired
    private OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    @Autowired
    private DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    @Autowired
    private DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    private Long cuestionarioId;

    @BeforeEach
    void setUp() {
        Temario cuestionario = new Temario();
        cuestionario.setTitulo("Cuestionario de prueba");
        cuestionario.setTipo(TipoAResponder.CUESTIONARIO);
        cuestionario.setFechaDeCreacion(LocalDateTime.now());
        cuestionarioId = temarioRepositoryPort.save(cuestionario).getId();
    }

    @Test
    void updateQuestion_preguntaSimple_actualizaYDevuelveElDominioHexagonal() throws Exception {
        PreguntaSimple original = new PreguntaSimple();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.PREGUNTA_SIMPLE);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setRespuestaEstablecida("respuesta original");
        Long id = preguntaSimpleRepositoryPort.save(original).getId();

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.PREGUNTA_SIMPLE,
                cuestionarioId, null, "respuesta editada", null, null, null, null);

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.respuestaEstablecida").value("respuesta editada"));

        PreguntaSimple persistida = preguntaSimpleRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getTitulo()).isEqualTo("Titulo editado");
        assertThat(persistida.getRespuestaEstablecida()).isEqualTo("respuesta editada");
    }

    @Test
    void updateQuestion_verdaderoOFalso_actualizaYDevuelveElDominioHexagonal() throws Exception {
        VerdaderoOFalso original = new VerdaderoOFalso();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.VERDADERO_FALSO);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setRespuestaVerdadera(true);
        Long id = verdaderoOFalsoRepositoryPort.save(original).getId();

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.VERDADERO_FALSO,
                cuestionarioId, false, null, null, null, null, null);

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.respuestaVerdadera").value(false));

        VerdaderoOFalso persistida = verdaderoOFalsoRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getRespuestaVerdadera()).isFalse();
    }

    @Test
    void updateQuestion_seleccionUnica_actualizaOpcionesYDevuelveElDominioHexagonal() throws Exception {
        Opcion opcionA = new Opcion();
        opcionA.setOpcion("Opcion A");
        opcionA.setLaRespuestaEs(true);
        Opcion opcionB = new Opcion();
        opcionB.setOpcion("Opcion B");
        opcionB.setLaRespuestaEs(false);

        SeleccionUnica original = new SeleccionUnica();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.SELECCION_UNICA);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setListaDeOpciones(List.of(opcionA, opcionB));
        SeleccionUnica creada = seleccionUnicaRepositoryPort.save(original);
        Long id = creada.getId();

        Opcion opcionAEditada = new Opcion();
        opcionAEditada.setId(creada.getListaDeOpciones().get(0).getId());
        opcionAEditada.setOpcion("Opcion A editada");
        opcionAEditada.setLaRespuestaEs(false);
        Opcion opcionBEditada = new Opcion();
        opcionBEditada.setId(creada.getListaDeOpciones().get(1).getId());
        opcionBEditada.setOpcion("Opcion B editada");
        opcionBEditada.setLaRespuestaEs(true);

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.SELECCION_UNICA,
                cuestionarioId, null, null, null, List.of(opcionAEditada, opcionBEditada), null, null);

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.listaDeOpciones[0].opcion").value("Opcion A editada"))
                .andExpect(jsonPath("$.listaDeOpciones[1].opcion").value("Opcion B editada"));

        SeleccionUnica persistida = seleccionUnicaRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getListaDeOpciones())
                .extracting(Opcion::getOpcion, Opcion::getLaRespuestaEs)
                .containsExactly(
                        tuple("Opcion A editada", false),
                        tuple("Opcion B editada", true)
                );
    }

    @Test
    void updateQuestion_opcionMultiple_actualizaOpcionesYDevuelveElDominioHexagonal() throws Exception {
        Opcion opcionA = new Opcion();
        opcionA.setOpcion("Opcion A");
        opcionA.setLaRespuestaEs(true);
        Opcion opcionB = new Opcion();
        opcionB.setOpcion("Opcion B");
        opcionB.setLaRespuestaEs(true);

        OpcionMultiple original = new OpcionMultiple();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.OPCION_MULTIPLE);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setListaDeOpciones(List.of(opcionA, opcionB));
        OpcionMultiple creada = opcionMultipleRepositoryPort.save(original);
        Long id = creada.getId();

        Opcion opcionAEditada = new Opcion();
        opcionAEditada.setId(creada.getListaDeOpciones().get(0).getId());
        opcionAEditada.setOpcion("Opcion A editada");
        opcionAEditada.setLaRespuestaEs(false);
        Opcion opcionBEditada = new Opcion();
        opcionBEditada.setId(creada.getListaDeOpciones().get(1).getId());
        opcionBEditada.setOpcion("Opcion B editada");
        opcionBEditada.setLaRespuestaEs(true);

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.OPCION_MULTIPLE,
                cuestionarioId, null, null, null, List.of(opcionAEditada, opcionBEditada), null, null);

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.listaDeOpciones[0].opcion").value("Opcion A editada"));

        OpcionMultiple persistida = opcionMultipleRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getListaDeOpciones())
                .extracting(Opcion::getOpcion)
                .containsExactly("Opcion A editada", "Opcion B editada");
    }

    @Test
    void updateQuestion_desplegableCompartido_actualizaOpcionesYDevuelveElDominioHexagonal() throws Exception {
        OpcionDeDesplegableCompartido opcion = new OpcionDeDesplegableCompartido();
        opcion.setPregunta("La capital de Francia es [desplegable]");
        opcion.setRespuesta("Paris");

        DesplegableCompartido original = new DesplegableCompartido();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.DESPLEGABLE_COMPARTIDO);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setListaDeOpciones(List.of(opcion));
        DesplegableCompartido creada = desplegableCompartidoRepositoryPort.save(original);
        Long id = creada.getId();

        OpcionDeDesplegableCompartido opcionEditada = new OpcionDeDesplegableCompartido();
        opcionEditada.setId(creada.getListaDeOpciones().get(0).getId());
        opcionEditada.setPregunta("La capital de Alemania es [desplegable]");
        opcionEditada.setRespuesta("Berlin");

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.DESPLEGABLE_COMPARTIDO,
                cuestionarioId, null, null, null, null, List.of(opcionEditada), null);

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.listaDeOpciones[0].respuesta").value("Berlin"));

        DesplegableCompartido persistida = desplegableCompartidoRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getListaDeOpciones())
                .extracting(OpcionDeDesplegableCompartido::getRespuesta)
                .containsExactly("Berlin");
    }

    @Test
    void updateQuestion_desplegableIndependiente_actualizaSubPreguntasYDevuelveElDominioHexagonal() throws Exception {
        Opcion opcionInterna = new Opcion();
        opcionInterna.setOpcion("Opcion interna");
        opcionInterna.setLaRespuestaEs(true);
        SeleccionUnicaParaDesplegableIndependiente subPregunta = new SeleccionUnicaParaDesplegableIndependiente();
        subPregunta.setTitulo("Sub pregunta original");
        subPregunta.setListaDeOpciones(List.of(opcionInterna));

        DesplegableIndependiente original = new DesplegableIndependiente();
        original.setTitulo("Titulo original");
        original.setDescripcion("Descripcion original");
        original.setTipo(TipoAResponder.DESPLEGABLE_INDEPENDIENTE);
        original.setIdDuenio(cuestionarioId);
        original.setFechaDeCreacion(LocalDateTime.now());
        original.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        original.setListaDeOpciones(List.of(subPregunta));
        DesplegableIndependiente creada = desplegableIndependienteRepositoryPort.save(original);
        Long id = creada.getId();

        SeleccionUnicaParaDesplegableIndependiente subPreguntaCreada = creada.getListaDeOpciones().get(0);
        Opcion opcionInternaEditada = new Opcion();
        opcionInternaEditada.setId(subPreguntaCreada.getListaDeOpciones().get(0).getId());
        opcionInternaEditada.setOpcion("Opcion interna editada");
        opcionInternaEditada.setLaRespuestaEs(false);
        SeleccionUnicaParaDesplegableIndependiente subPreguntaEditada = new SeleccionUnicaParaDesplegableIndependiente();
        subPreguntaEditada.setId(subPreguntaCreada.getId());
        subPreguntaEditada.setTitulo("Sub pregunta editada");
        subPreguntaEditada.setListaDeOpciones(List.of(opcionInternaEditada));

        PostPreguntaDTO edicion = new PostPreguntaDTO(id, "Titulo editado", "Descripcion editada", TipoAResponder.DESPLEGABLE_INDEPENDIENTE,
                cuestionarioId, null, null, null, null, null, List.of(subPreguntaEditada));

        mockMvc.perform(put("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo editado"))
                .andExpect(jsonPath("$.listaDeOpciones[0].titulo").value("Sub pregunta editada"))
                .andExpect(jsonPath("$.listaDeOpciones[0].listaDeOpciones[0].opcion").value("Opcion interna editada"));

        DesplegableIndependiente persistida = desplegableIndependienteRepositoryPort.findById(id).orElseThrow();
        assertThat(persistida.getListaDeOpciones().get(0).getTitulo()).isEqualTo("Sub pregunta editada");
        assertThat(persistida.getListaDeOpciones().get(0).getListaDeOpciones())
                .extracting(Opcion::getOpcion)
                .containsExactly("Opcion interna editada");
    }
}
