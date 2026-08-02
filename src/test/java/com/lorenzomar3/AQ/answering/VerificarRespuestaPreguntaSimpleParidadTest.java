package com.lorenzomar3.AQ.answering;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaSimpleRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaPreguntaSimpleCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaPreguntaSimpleHandler;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class VerificarRespuestaPreguntaSimpleParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaSimpleRepository preguntaSimpleRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private VerificarRespuestaPreguntaSimpleHandler verificarRespuestaPreguntaSimpleHandler;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba verify PreguntaSimple", TipoAResponder.CUESTIONARIO);
        temarioId = temarioRepository.save(temario).getId();
    }

    @AfterEach
    void limpiar() {
        preguntasCreadas.forEach(id -> {
            try {
                preguntaSimpleRepositoryViejo.deleteById(id);
            } catch (Exception ignored) {
            }
        });
        temarioRepository.deleteById(temarioId);
    }

    private Long obtenerUltimoHijo() {
        Temario temario = temarioRepository.findById(temarioId).orElseThrow();
        return temario.getListaAResponder().stream().mapToLong(AResponder::getId).max().orElseThrow();
    }

    private Long crearPreguntaSimple() {
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta de prueba", "Desc",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta de prueba", null, null, null, null));
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);
        return id;
    }

    @Test
    void respuestaCorrectaDevuelveElMismoBooleanoYElMismoContadorDeCriticoQueElFlujoViejo() {
        Long idViejo = crearPreguntaSimple();
        Long idNuevo = crearPreguntaSimple();

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.PREGUNTA_SIMPLE, null, true, null, null, null);
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.PREGUNTA_SIMPLE, null, true, null, null, null);

        Boolean resultadoViejo = preguntaServiceViejo.verifyResponse(respuestaViejo);
        Boolean resultadoNuevo = verificarRespuestaPreguntaSimpleHandler.ejecutar(
                new VerificarRespuestaPreguntaSimpleCommand(respuestaNuevo.idPregunta(), respuestaNuevo.respuestaBooleana()));

        assertEquals(resultadoViejo, resultadoNuevo);

        var entidadVieja = preguntaSimpleRepositoryViejo.findById(idViejo).orElseThrow();
        var entidadNueva = preguntaSimpleRepositoryViejo.findById(idNuevo).orElseThrow();
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void respuestaIncorrectaDevuelveElMismoBooleanoYElMismoContadorDeCriticoQueElFlujoViejo() {
        Long idViejo = crearPreguntaSimple();
        Long idNuevo = crearPreguntaSimple();

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.PREGUNTA_SIMPLE, null, false, null, null, null);
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.PREGUNTA_SIMPLE, null, false, null, null, null);

        Boolean resultadoViejo = preguntaServiceViejo.verifyResponse(respuestaViejo);
        Boolean resultadoNuevo = verificarRespuestaPreguntaSimpleHandler.ejecutar(
                new VerificarRespuestaPreguntaSimpleCommand(respuestaNuevo.idPregunta(), respuestaNuevo.respuestaBooleana()));

        assertEquals(resultadoViejo, resultadoNuevo);

        var entidadVieja = preguntaSimpleRepositoryViejo.findById(idViejo).orElseThrow();
        var entidadNueva = preguntaSimpleRepositoryViejo.findById(idNuevo).orElseThrow();
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void verificarConIdInexistenteLanzaBussinesException() {
        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(ID_INEXISTENTE, TipoAResponder.PREGUNTA_SIMPLE, null, true, null, null, null);

        assertThrows(BussinesException.class, () -> verificarRespuestaPreguntaSimpleHandler.ejecutar(
                new VerificarRespuestaPreguntaSimpleCommand(respuesta.idPregunta(), respuesta.respuestaBooleana())));
    }
}
