package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
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
class VerdaderoOFalsoParidadTest {

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private EditarVerdaderoOFalsoUseCase editarVerdaderoOFalsoUseCase;

    @Autowired
    private EliminarVerdaderoOFalsoUseCase eliminarVerdaderoOFalsoUseCase;

    @Autowired
    private VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad VerdaderoOFalso", TipoAResponder.CUESTIONARIO);
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

    @Test
    void crearVerdaderoOFalsoProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta de prueba", "Descripcion de prueba",
                TipoAResponder.VERDADERO_FALSO, temarioId, true, null, null, null, null, null);

        CreateQuestionResponseDTO respuestaVieja = preguntaServiceViejo.createaQuestion(dto);
        Long idPreguntaVieja = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaVieja);

        CreateQuestionResponseDTO respuestaNueva = crearVerdaderoOFalsoUseCase.crear(dto);
        Long idPreguntaNueva = obtenerUltimoHijo();
        preguntasCreadas.add(idPreguntaNueva);

        assertEquals(respuestaVieja, respuestaNueva);

        var entidadVieja = (com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.VerdaderoOFalso)
                preguntaRepositoryViejo.findById(idPreguntaVieja).orElseThrow();
        var entidadNueva = verdaderoOFalsoRepositoryPort.findById(idPreguntaNueva).orElseThrow();

        assertEquals(entidadVieja.getTitulo(), entidadNueva.getTitulo());
        assertEquals(entidadVieja.getDescripcion(), entidadNueva.getDescripcion());
        // El flujo viejo (BeanUtils.copyProperties sobre un record) nunca copia respuestaVerdadera: queda null.
        // Ver "Decisiones tomadas y descartadas" en el spec. El flujo nuevo sí persiste el valor correcto.
        assertEquals(null, entidadVieja.respuestaVerdadera);
        assertEquals(dto.respuestaVerdadera(), entidadNueva.getRespuestaVerdadera());
        assertEquals(entidadVieja.getTipo(), entidadNueva.getTipo());
        assertEquals(entidadVieja.getIdDuenio(), entidadNueva.getIdDuenio());
        assertEquals(entidadVieja.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNueva.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void editarVerdaderoOFalsoPreservaCriticosYProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dtoCreacion = new PostPreguntaDTO(null, "Original", "Desc original",
                TipoAResponder.VERDADERO_FALSO, temarioId, true, null, null, null, null, null);
        crearVerdaderoOFalsoUseCase.crear(dtoCreacion);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);

        var entidadPrevia = preguntaRepositoryViejo.findById(id).orElseThrow();
        entidadPrevia.setIntentosParaQueDejeDeSerCriticoDisponible(2);
        preguntaRepositoryViejo.save(entidadPrevia);

        PostPreguntaDTO dtoEdicion = new PostPreguntaDTO(id, "Editado", "Desc editada",
                TipoAResponder.VERDADERO_FALSO, temarioId, false, null, null, null, null, null);

        preguntaServiceViejo.updateQuestion(dtoEdicion);
        var entidadViejaEditada = (com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.VerdaderoOFalso)
                preguntaRepositoryViejo.findById(id).orElseThrow();

        var entidadNuevaEditada = editarVerdaderoOFalsoUseCase.editar(dtoEdicion);

        assertEquals(entidadViejaEditada.getTitulo(), entidadNuevaEditada.getTitulo());
        assertEquals(entidadViejaEditada.getDescripcion(), entidadNuevaEditada.getDescripcion());
        // El flujo viejo (BeanUtils.copyProperties sobre un record) nunca copia respuestaVerdadera al editar:
        // el campo queda intacto en su valor de creación (true), ignorando el false de dtoEdicion.
        // Ver "Decisiones tomadas y descartadas" en el spec. El flujo nuevo sí aplica la edición correctamente.
        assertEquals(true, entidadViejaEditada.respuestaVerdadera);
        assertEquals(dtoEdicion.respuestaVerdadera(), entidadNuevaEditada.getRespuestaVerdadera());
        assertEquals(entidadViejaEditada.getIntentosParaQueDejeDeSerCriticoDisponible(), entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
        assertEquals(2, entidadNuevaEditada.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void eliminarVerdaderoOFalsoUseCaseBorraLaPreguntaCreada() {
        PostPreguntaDTO dtoCreacion = new PostPreguntaDTO(null, "A borrar", "Desc",
                TipoAResponder.VERDADERO_FALSO, temarioId, true, null, null, null, null, null);
        crearVerdaderoOFalsoUseCase.crear(dtoCreacion);
        Long id = obtenerUltimoHijo();

        eliminarVerdaderoOFalsoUseCase.eliminar(id);

        assertTrue(verdaderoOFalsoRepositoryPort.findById(id).isEmpty());
    }
}
