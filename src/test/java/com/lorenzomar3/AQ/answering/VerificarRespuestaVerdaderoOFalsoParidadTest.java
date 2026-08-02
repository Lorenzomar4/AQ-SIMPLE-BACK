package com.lorenzomar3.AQ.answering;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.answering.application.port.in.VerificarRespuestaVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
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
class VerificarRespuestaVerdaderoOFalsoParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private VerificarRespuestaVerdaderoOFalsoUseCase verificarRespuestaVerdaderoOFalsoUseCase;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba verify VerdaderoOFalso", TipoAResponder.CUESTIONARIO);
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

    // Fixture creada vía el flujo nuevo (content): el flujo viejo (createaQuestion) tiene un bug conocido
    // que nunca persiste respuestaVerdadera (ver VerdaderoOFalsoParidadTest), lo que invalidaría esta comparación.
    private Long crearVerdaderoOFalso(boolean respuestaVerdadera) {
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Pregunta de prueba", "Desc",
                TipoAResponder.VERDADERO_FALSO, temarioId, respuestaVerdadera, null, null, null, null, null));
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);
        return id;
    }

    @Test
    void verificarRespuestaDevuelveElMismoBooleanoYElMismoContadorDeCriticoQueElFlujoViejoParaLasCuatroCombinaciones() {
        List<boolean[]> combinaciones = List.of(
                new boolean[]{true, true},
                new boolean[]{true, false},
                new boolean[]{false, true},
                new boolean[]{false, false}
        );

        for (boolean[] combinacion : combinaciones) {
            boolean respuestaVerdadera = combinacion[0];
            boolean respuestaDelUsuario = combinacion[1];

            Long idViejo = crearVerdaderoOFalso(respuestaVerdadera);
            Long idNuevo = crearVerdaderoOFalso(respuestaVerdadera);

            RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.VERDADERO_FALSO, null, respuestaDelUsuario, null, null, null);
            RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.VERDADERO_FALSO, null, respuestaDelUsuario, null, null, null);

            Boolean resultadoViejo = preguntaServiceViejo.verifyResponse(respuestaViejo);
            Boolean resultadoNuevo = verificarRespuestaVerdaderoOFalsoUseCase.verificar(respuestaNuevo);

            assertEquals(resultadoViejo, resultadoNuevo);

            var entidadVieja = preguntaRepositoryViejo.findById(idViejo).orElseThrow();
            var entidadNueva = preguntaRepositoryViejo.findById(idNuevo).orElseThrow();
            assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());
        }
    }

    @Test
    void verificarConIdInexistenteLanzaBussinesException() {
        RespuestaDePreguntaDTO respuesta = new RespuestaDePreguntaDTO(ID_INEXISTENTE, TipoAResponder.VERDADERO_FALSO, null, true, null, null, null);

        assertThrows(BussinesException.class, () -> verificarRespuestaVerdaderoOFalsoUseCase.verificar(respuesta));
    }
}
