package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.command.CrearPreguntaInversaCommand;
import com.lorenzomar3.AQ.content.application.command.CrearPreguntaInversaHandler;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.InverseQuestionCreateDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Compara POST /questions/inverse antes (PreguntaService.createInverseQuestion) y después
 * (CrearPreguntaInversaHandler) de migrarlo: en ambos caminos, invertir una PREGUNTA_SIMPLE
 * intercambia titulo <-> respuesta (limpiando HTML de la respuesta original con Jsoup) y agrega
 * la nueva pregunta como hija del mismo temario que la original. Invertir un VERDADERO_FALSO
 * (u otro tipo distinto de PREGUNTA_SIMPLE) no hace nada en ambos caminos.
 */
@SpringBootTest
class QuestionInverseParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearPreguntaInversaHandler crearPreguntaInversaHandler;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    private final List<Long> temariosCreados = new ArrayList<>();

    @AfterEach
    void limpiar() {
        temariosCreados.forEach(id -> {
            try {
                temarioRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        });
    }

    private Long crearTemario(String nombre) {
        Temario temario = new Temario(nombre, TipoAResponder.CUESTIONARIO);
        Long id = temarioRepository.save(temario).getId();
        temariosCreados.add(id);
        return id;
    }

    private Long obtenerUltimoHijo(Long temarioId) {
        Temario temario = temarioRepository.findById(temarioId).orElseThrow();
        return temario.getListaAResponder().stream().mapToLong(AResponder::getId).max().orElseThrow();
    }

    @Test
    void invertirPreguntaSimpleIntercambiaTituloYRespuestaLimpiandoHtmlIgualQueElFlujoViejo() {
        String tituloOriginal = "Titulo original";
        String respuestaOriginalConHtml = "<p>Respuesta <b>original</b></p>";
        String respuestaLimpiaEsperada = "Respuesta original";

        Long temarioViejoId = crearTemario("Temario viejo inverse");
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, tituloOriginal, "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                temarioViejoId, null, respuestaOriginalConHtml, null, null, null, null));
        Long preguntaOriginalViejaId = obtenerUltimoHijo(temarioViejoId);

        Long temarioNuevoId = crearTemario("Temario nuevo inverse");
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, tituloOriginal, "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                temarioNuevoId, null, respuestaOriginalConHtml, null, null, null, null));
        Long preguntaOriginalNuevaId = obtenerUltimoHijo(temarioNuevoId);

        preguntaServiceViejo.createInverseQuestion(new InverseQuestionCreateDTO(preguntaOriginalViejaId, TipoAResponder.PREGUNTA_SIMPLE));
        Long preguntaInvertidaViejaId = obtenerUltimoHijo(temarioViejoId);

        crearPreguntaInversaHandler.ejecutar(new CrearPreguntaInversaCommand(preguntaOriginalNuevaId, TipoAResponder.PREGUNTA_SIMPLE));
        Long preguntaInvertidaNuevaId = obtenerUltimoHijo(temarioNuevoId);

        var preguntaInvertidaVieja = preguntaSimpleRepositoryPort.findById(preguntaInvertidaViejaId).orElseThrow();
        var preguntaInvertidaNueva = preguntaSimpleRepositoryPort.findById(preguntaInvertidaNuevaId).orElseThrow();

        assertEquals(respuestaLimpiaEsperada, preguntaInvertidaVieja.getTitulo());
        assertEquals(tituloOriginal, preguntaInvertidaVieja.getRespuestaEstablecida());

        assertEquals(preguntaInvertidaVieja.getTitulo(), preguntaInvertidaNueva.getTitulo());
        assertEquals(preguntaInvertidaVieja.getRespuestaEstablecida(), preguntaInvertidaNueva.getRespuestaEstablecida());

        // Cada inversa cuelga de su propio temario (temarioViejoId / temarioNuevoId son fixtures
        // independientes, no tiene sentido comparar idDuenio entre lados).
        assertEquals(temarioViejoId, preguntaInvertidaVieja.getIdDuenio());
        assertEquals(temarioNuevoId, preguntaInvertidaNueva.getIdDuenio());
    }

    @Test
    void invertirUnTipoDistintoDePreguntaSimpleNoHaceNadaEnAmbosCaminos() {
        Long temarioViejoId = crearTemario("Temario viejo inverse VoF");
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "VoF", "Desc", TipoAResponder.VERDADERO_FALSO,
                temarioViejoId, true, null, null, null, null, null));
        Long vofViejoId = obtenerUltimoHijo(temarioViejoId);

        Long temarioNuevoId = crearTemario("Temario nuevo inverse VoF");
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "VoF", "Desc", TipoAResponder.VERDADERO_FALSO,
                temarioNuevoId, true, null, null, null, null, null));
        Long vofNuevoId = obtenerUltimoHijo(temarioNuevoId);

        preguntaServiceViejo.createInverseQuestion(new InverseQuestionCreateDTO(vofViejoId, TipoAResponder.VERDADERO_FALSO));
        crearPreguntaInversaHandler.ejecutar(new CrearPreguntaInversaCommand(vofNuevoId, TipoAResponder.VERDADERO_FALSO));

        int hijosViejo = temarioRepository.findById(temarioViejoId).orElseThrow().getListaAResponder().size();
        int hijosNuevo = temarioRepository.findById(temarioNuevoId).orElseThrow().getListaAResponder().size();

        assertEquals(1, hijosViejo);
        assertEquals(1, hijosNuevo);
    }
}
