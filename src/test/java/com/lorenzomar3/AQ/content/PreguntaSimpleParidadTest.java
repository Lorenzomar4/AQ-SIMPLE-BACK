package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaSimpleRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PreguntaSimpleParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaSimpleRepository preguntaSimpleRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private EditarPreguntaUseCase editarPreguntaUseCase;

    @Autowired
    private EliminarPreguntaUseCase eliminarPreguntaUseCase;

    @Autowired
    private PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad PreguntaSimple", TipoAResponder.CUESTIONARIO);
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

    @Test
    void crearPreguntaSimpleProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta de prueba", "Descripcion de prueba",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta de prueba", null, null, null, null);

        CreateQuestionResponseDTO respuestaVieja = preguntaServiceViejo.createaQuestion(dto);
        Long idPreguntaVieja = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaVieja);

        CreateQuestionResponseDTO respuestaNueva = crearPreguntaUseCase.crear(dto);
        Long idPreguntaNueva = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaNueva);

        assertEquals(respuestaVieja, respuestaNueva);

        var entidadVieja = preguntaSimpleRepositoryViejo.findById(idPreguntaVieja).orElseThrow();
        var entidadNueva = preguntaSimpleRepositoryPort.findById(idPreguntaNueva).orElseThrow();

        assertEquals(entidadVieja.getTitulo(), entidadNueva.getTitulo());
        assertEquals(entidadVieja.getDescripcion(), entidadNueva.getDescripcion());
        assertEquals(entidadVieja.getRespuestaEstablecida(), entidadNueva.getRespuestaEstablecida());
        assertEquals(entidadVieja.getTipo(), entidadNueva.getTipo());
        assertEquals(entidadVieja.getIdDuenio(), entidadNueva.getIdDuenio());
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(entidadVieja.getRespuestaPrecisa(), entidadNueva.getRespuestaPrecisa());
    }

    @Test
    void editarPreguntaSimplePreservaCriticosYProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dtoCreacion = new PostPreguntaDTO(null, "Original", "Desc original",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta original", null, null, null, null);
        crearPreguntaUseCase.crear(dtoCreacion);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        var entidadPrevia = preguntaSimpleRepositoryViejo.findById(id).orElseThrow();
        entidadPrevia.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaSimpleRepositoryViejo.save(entidadPrevia);

        PostPreguntaDTO dtoEdicion = new PostPreguntaDTO(id, "Editado", "Desc editada",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta editada", null, null, null, null);

        preguntaServiceViejo.updateQuestion(dtoEdicion);
        var entidadViejaEditada = preguntaSimpleRepositoryViejo.findById(id).orElseThrow();

        var entidadNuevaEditada = editarPreguntaUseCase.editar(dtoEdicion);

        assertEquals(entidadViejaEditada.getTitulo(), entidadNuevaEditada.getTitulo());
        assertEquals(entidadViejaEditada.getDescripcion(), entidadNuevaEditada.getDescripcion());
        assertEquals(entidadViejaEditada.getRespuestaEstablecida(), entidadNuevaEditada.getRespuestaEstablecida());
        assertEquals(entidadViejaEditada.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(2, entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(entidadViejaEditada.getRespuestaPrecisa(), entidadNuevaEditada.getRespuestaPrecisa());
    }

    @Test
    void eliminarPreguntaUseCaseBorraLaPreguntaCreada() {
        PostPreguntaDTO dtoCreacion = new PostPreguntaDTO(null, "A borrar", "Desc",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta", null, null, null, null);
        crearPreguntaUseCase.crear(dtoCreacion);
        Long id = obtenerUltimoHijo();

        eliminarPreguntaUseCase.eliminar(id);

        assertTrue(preguntaSimpleRepositoryPort.findById(id).isEmpty());
    }
}
