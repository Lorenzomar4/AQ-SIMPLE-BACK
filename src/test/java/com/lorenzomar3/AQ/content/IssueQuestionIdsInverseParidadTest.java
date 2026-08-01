package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.content.application.port.in.CrearIssueInversoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.InverseIssueCreateDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class IssueQuestionIdsInverseParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepositoryViejo;

    @Autowired
    private TemarioService temarioServiceViejo;

    @Autowired
    private ObtenerIdsDePreguntasUseCase obtenerIdsDePreguntasUseCase;

    @Autowired
    private CrearIssueInversoUseCase crearIssueInversoUseCase;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private TemarioRepositoryPort temarioRepositoryPort;

    @Autowired
    private PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    private final List<Long> cuestionariosCreados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        cuestionariosCreados.forEach(id -> {
            try {
                temarioRepositoryViejo.deleteById(id);
            } catch (Exception ignored) {
            }
        });
    }

    private Long obtenerUltimoHijo(Long padreId) {
        Temario padre = temarioRepositoryViejo.findById(padreId).orElseThrow();
        return padre.getListaAResponder().stream().mapToLong(AResponder::getId).max().orElseThrow();
    }

    @Test
    void obtenerIdsDePreguntasRecogeTodasLasHojasConSubtemasAnidadosYTiposMixtosIgualQueElFlujoViejo() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario para ids"));
        Long cuestionarioId = cuestionario.getId();
        cuestionariosCreados.add(cuestionarioId);

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Tema", null, cuestionarioId));
        Long temaId = obtenerUltimoHijo(cuestionarioId);

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema", null, temaId));
        Long subtemaId = obtenerUltimoHijo(temaId);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta raiz", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                cuestionarioId, null, "Respuesta raiz", null, null, null, null));
        Long preguntaRaizId = obtenerUltimoHijo(cuestionarioId);

        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Pregunta tema", "Desc", TipoAResponder.VERDADERO_FALSO,
                temaId, true, null, null, null, null, null));
        Long preguntaTemaId = obtenerUltimoHijo(temaId);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta subtema", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                subtemaId, null, "Respuesta subtema", null, null, null, null));
        Long preguntaSubtemaId = obtenerUltimoHijo(subtemaId);

        List<Long> idsViejo = temarioServiceViejo.obtenerTodosLosIdsDePreguntas(cuestionarioId);
        List<Long> idsNuevo = obtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(cuestionarioId);

        Set<Long> idsEsperados = Set.of(preguntaRaizId, preguntaTemaId, preguntaSubtemaId);

        assertEquals(idsEsperados, Set.copyOf(idsViejo));
        assertEquals(idsEsperados, Set.copyOf(idsNuevo));
        assertEquals(idsViejo.size(), idsNuevo.size());
    }

    @Test
    void obtenerIdsDePreguntasLanzaBussinesExceptionSiElTemarioNoExisteEnAmbosCaminos() {
        BussinesException excepcionVieja = assertThrows(BussinesException.class, () ->
                temarioServiceViejo.obtenerTodosLosIdsDePreguntas(ID_INEXISTENTE));

        BussinesException excepcionNueva = assertThrows(BussinesException.class, () ->
                obtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(ID_INEXISTENTE));

        assertEquals(excepcionVieja.getMessage(), excepcionNueva.getMessage());
    }

    @Test
    void crearIssueInversoInviertePreguntaSimpleDirectaIgnorandoOtrosTiposYSubtemasIgualQueElFlujoViejo() {
        String tituloOriginal = "Titulo original";
        String respuestaOriginalConHtml = "<p>Respuesta <b>original</b></p>";
        String tituloInvertidoEsperado = "Respuesta original";

        Temario padreViejo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Padre viejo"));
        cuestionariosCreados.add(padreViejo.getId());
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, tituloOriginal, "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                padreViejo.getId(), null, respuestaOriginalConHtml, null, null, null, null));
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "VoF ignorado", "Desc", TipoAResponder.VERDADERO_FALSO,
                padreViejo.getId(), true, null, null, null, null, null));
        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema ignorado", null, padreViejo.getId()));

        Temario padreNuevo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Padre nuevo"));
        cuestionariosCreados.add(padreNuevo.getId());
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, tituloOriginal, "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                padreNuevo.getId(), null, respuestaOriginalConHtml, null, null, null, null));
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "VoF ignorado", "Desc", TipoAResponder.VERDADERO_FALSO,
                padreNuevo.getId(), true, null, null, null, null, null));
        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema ignorado", null, padreNuevo.getId()));

        Temario invertidoViejo = temarioServiceViejo.crearTemarioPreguntasInversa(
                new InverseIssueCreateDTO(padreViejo.getId(), "Invertido viejo"));
        cuestionariosCreados.add(invertidoViejo.getId());

        AResponderItemListDTO invertidoNuevo = crearIssueInversoUseCase.crear(
                new InverseIssueCreateDTO(padreNuevo.getId(), "Invertido nuevo"));
        cuestionariosCreados.add(invertidoNuevo.id());

        // Relacion de hermano: mismo idDuenio y tipo que el padre original, en ambos caminos.
        assertEquals(padreViejo.getIdDuenio(), invertidoViejo.getIdDuenio());
        assertEquals(padreViejo.getTipo(), invertidoViejo.getTipo());
        assertEquals(padreNuevo.getIdDuenio(), temarioRepositoryPort.findById(invertidoNuevo.id()).orElseThrow().getIdDuenio());
        assertEquals(invertidoViejo.getTipo(), invertidoNuevo.type());

        // Solo se invierte la PREGUNTA_SIMPLE directa: VoF y subtema quedan afuera.
        List<AResponder> hijosInvertidoViejo = invertidoViejo.getListaAResponder();
        assertEquals(1, hijosInvertidoViejo.size());

        Temario invertidoNuevoCompleto = temarioRepositoryViejo.findById(invertidoNuevo.id()).orElseThrow();
        assertEquals(1, invertidoNuevoCompleto.getListaAResponder().size());

        PreguntaSimple preguntaInvertidaVieja = (PreguntaSimple) hijosInvertidoViejo.get(0);
        Long preguntaInvertidaNuevaId = invertidoNuevoCompleto.getListaAResponder().get(0).getId();
        var preguntaInvertidaNueva = preguntaSimpleRepositoryPort.findById(preguntaInvertidaNuevaId).orElseThrow();

        assertEquals(tituloInvertidoEsperado, preguntaInvertidaVieja.getTitulo());
        assertEquals(tituloOriginal, preguntaInvertidaVieja.getRespuestaEstablecida());

        assertEquals(preguntaInvertidaVieja.getTitulo(), preguntaInvertidaNueva.getTitulo());
        assertEquals(preguntaInvertidaVieja.getRespuestaEstablecida(), preguntaInvertidaNueva.getRespuestaEstablecida());
    }

    @Test
    void crearIssueInversoLanzaBussinesExceptionSiElTemarioNoExisteEnAmbosCaminos() {
        BussinesException excepcionVieja = assertThrows(BussinesException.class, () ->
                temarioServiceViejo.crearTemarioPreguntasInversa(new InverseIssueCreateDTO(ID_INEXISTENTE, "No importa")));

        BussinesException excepcionNueva = assertThrows(BussinesException.class, () ->
                crearIssueInversoUseCase.crear(new InverseIssueCreateDTO(ID_INEXISTENTE, "No importa")));

        assertEquals(excepcionVieja.getMessage(), excepcionNueva.getMessage());
    }
}
