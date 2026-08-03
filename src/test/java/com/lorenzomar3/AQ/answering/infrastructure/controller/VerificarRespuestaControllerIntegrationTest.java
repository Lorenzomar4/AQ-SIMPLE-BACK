package com.lorenzomar3.AQ.answering.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.answering.infrastructure.controller.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VerificarRespuestaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemarioRepositoryPort temarioRepositoryPort;

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
    void verify_seleccionUnica_respuestaCorrectaDevuelveTrue() throws Exception {
        Opcion opcionA = new Opcion();
        opcionA.setOpcion("Opcion A");
        opcionA.setLaRespuestaEs(true);
        Opcion opcionB = new Opcion();
        opcionB.setOpcion("Opcion B");
        opcionB.setLaRespuestaEs(false);

        SeleccionUnica pregunta = new SeleccionUnica();
        pregunta.setTitulo("Pregunta de seleccion unica");
        pregunta.setTipo(TipoAResponder.SELECCION_UNICA);
        pregunta.setIdDuenio(cuestionarioId);
        pregunta.setFechaDeCreacion(LocalDateTime.now());
        pregunta.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        pregunta.setListaDeOpciones(List.of(opcionA, opcionB));
        SeleccionUnica creada = seleccionUnicaRepositoryPort.save(pregunta);

        Opcion respuestaA = new Opcion();
        respuestaA.setId(creada.getListaDeOpciones().get(0).getId());
        respuestaA.setLaRespuestaEs(true);
        Opcion respuestaB = new Opcion();
        respuestaB.setId(creada.getListaDeOpciones().get(1).getId());
        respuestaB.setLaRespuestaEs(false);

        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(creada.getId(), TipoAResponder.SELECCION_UNICA, null, null,
                List.of(respuestaA, respuestaB), null, null);

        mockMvc.perform(post("/questions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void verify_opcionMultiple_respuestaCorrectaDevuelveTrue() throws Exception {
        Opcion opcionA = new Opcion();
        opcionA.setOpcion("Opcion A");
        opcionA.setLaRespuestaEs(true);
        Opcion opcionB = new Opcion();
        opcionB.setOpcion("Opcion B");
        opcionB.setLaRespuestaEs(true);

        OpcionMultiple pregunta = new OpcionMultiple();
        pregunta.setTitulo("Pregunta de opcion multiple");
        pregunta.setTipo(TipoAResponder.OPCION_MULTIPLE);
        pregunta.setIdDuenio(cuestionarioId);
        pregunta.setFechaDeCreacion(LocalDateTime.now());
        pregunta.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        pregunta.setListaDeOpciones(List.of(opcionA, opcionB));
        OpcionMultiple creada = opcionMultipleRepositoryPort.save(pregunta);

        Opcion respuestaA = new Opcion();
        respuestaA.setId(creada.getListaDeOpciones().get(0).getId());
        respuestaA.setLaRespuestaEs(true);
        Opcion respuestaB = new Opcion();
        respuestaB.setId(creada.getListaDeOpciones().get(1).getId());
        respuestaB.setLaRespuestaEs(true);

        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(creada.getId(), TipoAResponder.OPCION_MULTIPLE, null, null,
                List.of(respuestaA, respuestaB), null, null);

        mockMvc.perform(post("/questions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void verify_desplegableCompartido_respuestaCorrectaDevuelveTrue() throws Exception {
        OpcionDeDesplegableCompartido opcion = new OpcionDeDesplegableCompartido();
        opcion.setPregunta("La capital de Francia es [desplegable]");
        opcion.setRespuesta("Paris");

        DesplegableCompartido pregunta = new DesplegableCompartido();
        pregunta.setTitulo("Pregunta de desplegable compartido");
        pregunta.setTipo(TipoAResponder.DESPLEGABLE_COMPARTIDO);
        pregunta.setIdDuenio(cuestionarioId);
        pregunta.setFechaDeCreacion(LocalDateTime.now());
        pregunta.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        pregunta.setListaDeOpciones(List.of(opcion));
        DesplegableCompartido creada = desplegableCompartidoRepositoryPort.save(pregunta);

        OpcionDeDesplegableCompartido respuestaOpcion = new OpcionDeDesplegableCompartido();
        respuestaOpcion.setId(creada.getListaDeOpciones().get(0).getId());
        respuestaOpcion.setRespuesta("Paris");

        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(creada.getId(), TipoAResponder.DESPLEGABLE_COMPARTIDO, null, null,
                null, List.of(respuestaOpcion), null);

        mockMvc.perform(post("/questions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void verify_desplegableIndependiente_respuestaCorrectaDevuelveTrue_ejercitaElGetterCorregido() throws Exception {
        Opcion opcionInterna = new Opcion();
        opcionInterna.setOpcion("Opcion interna");
        opcionInterna.setLaRespuestaEs(true);
        SeleccionUnicaParaDesplegableIndependiente subPregunta = new SeleccionUnicaParaDesplegableIndependiente();
        subPregunta.setTitulo("Sub pregunta");
        subPregunta.setListaDeOpciones(List.of(opcionInterna));

        DesplegableIndependiente pregunta = new DesplegableIndependiente();
        pregunta.setTitulo("Pregunta de desplegable independiente");
        pregunta.setTipo(TipoAResponder.DESPLEGABLE_INDEPENDIENTE);
        pregunta.setIdDuenio(cuestionarioId);
        pregunta.setFechaDeCreacion(LocalDateTime.now());
        pregunta.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        pregunta.setListaDeOpciones(List.of(subPregunta));
        DesplegableIndependiente creada = desplegableIndependienteRepositoryPort.save(pregunta);

        SeleccionUnicaParaDesplegableIndependiente subPreguntaCreada = creada.getListaDeOpciones().get(0);
        Opcion respuestaOpcionInterna = new Opcion();
        respuestaOpcionInterna.setId(subPreguntaCreada.getListaDeOpciones().get(0).getId());
        respuestaOpcionInterna.setLaRespuestaEs(true);
        SeleccionUnicaParaDesplegableIndependiente respuestaSubPregunta = new SeleccionUnicaParaDesplegableIndependiente();
        respuestaSubPregunta.setId(subPreguntaCreada.getId());
        respuestaSubPregunta.setListaDeOpciones(List.of(respuestaOpcionInterna));

        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(creada.getId(), TipoAResponder.DESPLEGABLE_INDEPENDIENTE, null, null,
                null, null, List.of(respuestaSubPregunta));

        mockMvc.perform(post("/questions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuesta)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
