package com.lorenzomar3.AQ.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaSimpleRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaFullUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaUseCase;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PreguntaSimpleFetchDTO;
import com.lorenzomar3.AQ.dto.newDto.PreguntaSimpleFullDTO;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ObtenerPreguntaSimpleContentTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaSimpleRepository preguntaSimpleRepositoryViejo;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private ObtenerPreguntaUseCase obtenerPreguntaUseCase;

    @Autowired
    private ObtenerPreguntaFullUseCase obtenerPreguntaFullUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba ObtenerPreguntaSimple", TipoAResponder.CUESTIONARIO);
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

    private Long crearPreguntaSimpleDePrueba() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta de prueba", "Descripcion de prueba",
                TipoAResponder.PREGUNTA_SIMPLE, temarioId, null, "Respuesta secreta", null, null, null, null);
        crearPreguntaUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);
        return id;
    }

    @Test
    void obtenerDevuelveLosCamposComunesPeroNoLaRespuestaEstablecida() throws Exception {
        Long id = crearPreguntaSimpleDePrueba();

        PreguntaSimpleFetchDTO dto = obtenerPreguntaUseCase.obtener(id);

        assertEquals(id, dto.id());
        assertEquals("Pregunta de prueba", dto.titulo());
        assertEquals("Descripcion de prueba", dto.descripcion());
        assertEquals(TipoAResponder.PREGUNTA_SIMPLE, dto.tipo());
        assertEquals(temarioId, dto.idDuenio());

        String json = objectMapper.writeValueAsString(dto);
        assertFalse(json.contains("respuestaEstablecida"),
                "La vista fetch no debe filtrar respuestaEstablecida bajo ningún nombre");
        assertFalse(json.contains("Respuesta secreta"),
                "El valor de la respuesta establecida no debe aparecer en la vista fetch");
    }

    @Test
    void obtenerFullIncluyeLaRespuestaEstablecidaConElValorCorrecto() {
        Long id = crearPreguntaSimpleDePrueba();

        PreguntaSimpleFullDTO dto = obtenerPreguntaFullUseCase.obtenerFull(id);

        assertEquals(id, dto.id());
        assertEquals("Pregunta de prueba", dto.titulo());
        assertEquals("Descripcion de prueba", dto.descripcion());
        assertEquals(TipoAResponder.PREGUNTA_SIMPLE, dto.tipo());
        assertEquals(temarioId, dto.idDuenio());
        assertEquals("Respuesta secreta", dto.respuestaEstablecida());
    }

    @Test
    void obtenerConIdInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> obtenerPreguntaUseCase.obtener(ID_INEXISTENTE));
    }

    @Test
    void obtenerFullConIdInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> obtenerPreguntaFullUseCase.obtenerFull(ID_INEXISTENTE));
    }
}
