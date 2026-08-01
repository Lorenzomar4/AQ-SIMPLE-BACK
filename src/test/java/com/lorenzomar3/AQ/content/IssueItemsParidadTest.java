package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerItemsDeIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.AResponderItemDetail;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.application.service.ObtenerItemsDeIssueService;
import com.lorenzomar3.AQ.dto.newDto.IssueWhitItemsDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class IssueItemsParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepositoryViejo;

    @Autowired
    private TemarioService temarioServiceViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private ObtenerItemsDeIssueUseCase obtenerItemsDeIssueUseCase;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

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
    void obtenerItemsDevuelveLaMismaRaizYElMismoItemListQueElFlujoViejoConHijosMixtosYSubtema() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario para items"));
        Long cuestionarioId = cuestionario.getId();
        cuestionariosCreados.add(cuestionarioId);

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Tema con items mixtos", null, cuestionarioId));
        Long temaId = obtenerUltimoHijo(cuestionarioId);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta simple del tema", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                temaId, null, "Respuesta simple", null, null, null, null));

        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Pregunta VoF del tema", "Desc", TipoAResponder.VERDADERO_FALSO,
                temaId, true, null, null, null, null, null));

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema del tema", null, temaId));
        Long subtemaId = obtenerUltimoHijo(temaId);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta del subtema", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                subtemaId, null, "Respuesta subtema", null, null, null, null));

        IssueWhitItemsDTO respuestaVieja = preguntaServiceViejo.getIssueItems(temaId);
        IssueWhitItemsDTO respuestaNueva = obtenerItemsDeIssueUseCase.obtenerItems(temaId);

        assertEquals(respuestaVieja.id(), respuestaNueva.id());
        assertEquals(respuestaVieja.name(), respuestaNueva.name());
        assertEquals(respuestaVieja.creationDate(), respuestaNueva.creationDate());
        assertEquals(respuestaVieja.fatherid(), respuestaNueva.fatherid());
        assertEquals(respuestaVieja.type(), respuestaNueva.type());
        assertEquals(respuestaVieja.isCritic(), respuestaNueva.isCritic());

        assertEquals(Set.copyOf(respuestaVieja.itemList()), Set.copyOf(respuestaNueva.itemList()));
        assertEquals(respuestaVieja.itemList().size(), respuestaNueva.itemList().size());
    }

    @Test
    void obtenerItemsLanzaBussinesExceptionSiElIssueNoExisteEnAmbosCaminos() {
        BussinesException excepcionVieja = assertThrows(BussinesException.class, () ->
                preguntaServiceViejo.getIssueItems(ID_INEXISTENTE));

        BussinesException excepcionNueva = assertThrows(BussinesException.class, () ->
                obtenerItemsDeIssueUseCase.obtenerItems(ID_INEXISTENTE));

        assertEquals(excepcionVieja.getMessage(), excepcionNueva.getMessage());
    }

    @Test
    void obtenerItemsPropagaNoSuchElementExceptionSiLaRaizNoApareceEnFindIssueItems() {
        Long id = 1L;
        com.lorenzomar3.AQ.content.domain.Temario raiz = new com.lorenzomar3.AQ.content.domain.Temario();
        raiz.setId(id);
        raiz.setTitulo("Raiz sin fila en findIssueItems");
        raiz.setTipo(TipoAResponder.CUESTIONARIO);

        TemarioRepositoryPort temarioRepositoryPortMock = mock(TemarioRepositoryPort.class);
        when(temarioRepositoryPortMock.findById(id)).thenReturn(Optional.of(raiz));
        when(temarioRepositoryPortMock.findIssueItems(id)).thenReturn(List.of(
                new AResponderItemDetail(2L, TipoAResponder.PREGUNTA_SIMPLE, "Otro item", null, false, null)
        ));

        ObtenerItemsDeIssueService servicio = new ObtenerItemsDeIssueService(temarioRepositoryPortMock);

        assertThrows(NoSuchElementException.class, () -> servicio.obtenerItems(id));
    }
}
