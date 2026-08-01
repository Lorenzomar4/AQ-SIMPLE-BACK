package com.lorenzomar3.AQ.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoFullUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.VerdaderoOFalsoFetchDTO;
import com.lorenzomar3.AQ.dto.newDto.VerdaderoOFalsoFullDTO;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ObtenerVerdaderoOFalsoContentTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private ObtenerVerdaderoOFalsoUseCase obtenerVerdaderoOFalsoUseCase;

    @Autowired
    private ObtenerVerdaderoOFalsoFullUseCase obtenerVerdaderoOFalsoFullUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba ObtenerVerdaderoOFalso", TipoAResponder.CUESTIONARIO);
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

    private Long crearVerdaderoOFalsoDePrueba() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta de prueba", "Descripcion de prueba",
                TipoAResponder.VERDADERO_FALSO, temarioId, true, null, null, null, null, null);
        crearVerdaderoOFalsoUseCase.crear(dto);
        Long id = obtenerUltimoHijo();
        preguntasCreadas.add(id);
        return id;
    }

    @Test
    void obtenerDevuelveLosCamposComunesPeroNoLaRespuestaVerdadera() throws Exception {
        Long id = crearVerdaderoOFalsoDePrueba();

        VerdaderoOFalsoFetchDTO dto = obtenerVerdaderoOFalsoUseCase.obtener(id);

        assertEquals(id, dto.id());
        assertEquals("Pregunta de prueba", dto.titulo());
        assertEquals("Descripcion de prueba", dto.descripcion());
        assertEquals(TipoAResponder.VERDADERO_FALSO, dto.tipo());
        assertEquals(temarioId, dto.idDuenio());

        String json = objectMapper.writeValueAsString(dto);
        assertFalse(json.contains("respuestaVerdadera"),
                "La vista fetch no debe filtrar respuestaVerdadera bajo ningún nombre");
    }

    @Test
    void obtenerFullIncluyeLaRespuestaVerdaderaConElValorCorrecto() {
        Long id = crearVerdaderoOFalsoDePrueba();

        VerdaderoOFalsoFullDTO dto = obtenerVerdaderoOFalsoFullUseCase.obtenerFull(id);

        assertEquals(id, dto.id());
        assertEquals("Pregunta de prueba", dto.titulo());
        assertEquals("Descripcion de prueba", dto.descripcion());
        assertEquals(TipoAResponder.VERDADERO_FALSO, dto.tipo());
        assertEquals(temarioId, dto.idDuenio());
        assertTrue(dto.respuestaVerdadera());
    }

    @Test
    void obtenerConIdInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> obtenerVerdaderoOFalsoUseCase.obtener(ID_INEXISTENTE));
    }

    @Test
    void obtenerFullConIdInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> obtenerVerdaderoOFalsoFullUseCase.obtenerFull(ID_INEXISTENTE));
    }
}
