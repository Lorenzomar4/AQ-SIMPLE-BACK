package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.content.application.port.in.CrearCuestionarioUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class IssueParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepositoryViejo;

    @Autowired
    private TemarioService temarioServiceViejo;

    @Autowired
    private CrearCuestionarioUseCase crearCuestionarioUseCase;

    @Autowired
    private CrearIssueUseCase crearIssueUseCase;

    @Autowired
    private EditarIssueUseCase editarIssueUseCase;

    @Autowired
    private EliminarIssueUseCase eliminarIssueUseCase;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private TemarioRepositoryPort temarioRepositoryPort;

    @Autowired
    private VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

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
    void crearCuestionarioProduceElMismoResultadoQueElFlujoViejo() {
        Temario cuestionarioViejo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario viejo"));
        cuestionariosCreados.add(cuestionarioViejo.getId());

        TemarioBasicDTO cuestionarioNuevo = crearCuestionarioUseCase.crear(new TemarioBasicDTO(null, "Cuestionario nuevo", null, null));
        cuestionariosCreados.add(cuestionarioNuevo.id());

        assertNotNull(cuestionarioNuevo.id());
        assertEquals("Cuestionario nuevo", cuestionarioNuevo.name());
        assertEquals(TipoAResponder.CUESTIONARIO, temarioRepositoryPort.findById(cuestionarioNuevo.id()).orElseThrow().getTipo());
        assertEquals(cuestionarioViejo.getTipo(), temarioRepositoryPort.findById(cuestionarioNuevo.id()).orElseThrow().getTipo());
    }

    @Test
    void crearIssueAsignaTemaBajoCuestionarioYSubtemaBajoTemaIgualQueElFlujoViejo() {
        Temario cuestionarioViejo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario viejo padre"));
        cuestionariosCreados.add(cuestionarioViejo.getId());

        Temario cuestionarioNuevo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario nuevo padre"));
        cuestionariosCreados.add(cuestionarioNuevo.getId());

        // Hijo bajo CUESTIONARIO -> debe quedar TEMA en ambos caminos
        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Tema viejo", null, cuestionarioViejo.getId()));
        Long temaViejoId = obtenerUltimoHijo(cuestionarioViejo.getId());
        TipoAResponder tipoTemaViejo = temarioRepositoryViejo.findByIdEssential(temaViejoId).orElseThrow().getTipo();

        AResponderItemListDTO temaNuevo = crearIssueUseCase.crear(new TemarioBasicDTO(null, "Tema nuevo", null, cuestionarioNuevo.getId()));

        assertEquals(TipoAResponder.TEMA, tipoTemaViejo);
        assertEquals(tipoTemaViejo, temaNuevo.type());
        assertNull(temaNuevo.numberOfQuestions());
        assertEquals(Boolean.FALSE, temaNuevo.isCritic());

        // Hijo bajo TEMA -> debe quedar SUBTEMA en ambos caminos
        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema viejo", null, temaViejoId));
        Long subtemaViejoId = obtenerUltimoHijo(temaViejoId);
        TipoAResponder tipoSubtemaViejo = temarioRepositoryViejo.findByIdEssential(subtemaViejoId).orElseThrow().getTipo();

        AResponderItemListDTO subtemaNuevo = crearIssueUseCase.crear(new TemarioBasicDTO(null, "Subtema nuevo", null, temaNuevo.id()));

        assertEquals(TipoAResponder.SUBTEMA, tipoSubtemaViejo);
        assertEquals(tipoSubtemaViejo, subtemaNuevo.type());
    }

    @Test
    void crearIssueBajoSubtemaLanzaBussinesExceptionEnAmbosCaminos() {
        Temario cuestionarioViejo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario para subtema"));
        cuestionariosCreados.add(cuestionarioViejo.getId());

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Tema", null, cuestionarioViejo.getId()));
        Long temaId = obtenerUltimoHijo(cuestionarioViejo.getId());

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema", null, temaId));
        Long subtemaId = obtenerUltimoHijo(temaId);

        BussinesException excepcionVieja = assertThrows(BussinesException.class, () ->
                temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Hijo invalido", null, subtemaId)));

        BussinesException excepcionNueva = assertThrows(BussinesException.class, () ->
                crearIssueUseCase.crear(new TemarioBasicDTO(null, "Hijo invalido", null, subtemaId)));

        assertEquals(excepcionVieja.getMessage(), excepcionNueva.getMessage());
        assertEquals("No se puede agregar un subtema a otro subtema", excepcionNueva.getMessage());
    }

    @Test
    void editarIssueActualizaTituloYUltimaActualizacionIgualQueElFlujoViejo() {
        Temario cuestionarioViejo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Titulo original viejo"));
        cuestionariosCreados.add(cuestionarioViejo.getId());

        Temario cuestionarioNuevo = temarioServiceViejo.saveTemarioCuestionario(new Temario("Titulo original nuevo"));
        cuestionariosCreados.add(cuestionarioNuevo.getId());

        Temario editadoViejo = temarioServiceViejo.actualizarCuestionario(
                new TemarioBasicDTO(cuestionarioViejo.getId(), "Titulo editado", null, null));

        TemarioBasicDTO editadoNuevo = editarIssueUseCase.editar(
                new TemarioBasicDTO(cuestionarioNuevo.getId(), "Titulo editado", null, null));

        assertEquals(editadoViejo.getTitulo(), editadoNuevo.name());
        assertEquals("Titulo editado", editadoNuevo.name());
        assertNotNull(temarioRepositoryViejo.findByIdEssential(cuestionarioNuevo.getId()).orElseThrow().getUltimaActualizacion());
    }

    @Test
    void editarIssueInexistenteLanzaBussinesExceptionEnAmbosCaminos() {
        BussinesException excepcionVieja = assertThrows(BussinesException.class, () ->
                temarioServiceViejo.actualizarCuestionario(new TemarioBasicDTO(ID_INEXISTENTE, "No importa", null, null)));

        BussinesException excepcionNueva = assertThrows(BussinesException.class, () ->
                editarIssueUseCase.editar(new TemarioBasicDTO(ID_INEXISTENTE, "No importa", null, null)));

        assertEquals(excepcionVieja.getMessage(), excepcionNueva.getMessage());
    }

    @Test
    void eliminarIssueInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> eliminarIssueUseCase.eliminar(ID_INEXISTENTE));
    }

    @Test
    void eliminarIssueSinHijosBorraElRegistro() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("A borrar sin hijos"));
        Long id = cuestionario.getId();

        eliminarIssueUseCase.eliminar(id);

        assertTrue(temarioRepositoryPort.findById(id).isEmpty());
    }

    @Test
    void eliminarIssueConHijosBorraEnCascadaSinViolarElFk() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario con hijos"));
        Long cuestionarioId = cuestionario.getId();

        AResponderItemListDTO tema = crearIssueUseCase.crear(new TemarioBasicDTO(null, "Tema", null, cuestionarioId));
        AResponderItemListDTO subtema = crearIssueUseCase.crear(new TemarioBasicDTO(null, "Subtema", null, tema.id()));

        PostPreguntaDTO preguntaDTO = new PostPreguntaDTO(null, "Pregunta bajo tema", "Descripcion", TipoAResponder.VERDADERO_FALSO,
                tema.id(), true, null, null, null, null, null);
        crearVerdaderoOFalsoUseCase.crear(preguntaDTO);
        Long preguntaId = obtenerUltimoHijo(tema.id());

        eliminarIssueUseCase.eliminar(cuestionarioId);

        assertTrue(temarioRepositoryPort.findById(cuestionarioId).isEmpty());
        assertTrue(temarioRepositoryPort.findById(tema.id()).isEmpty());
        assertTrue(temarioRepositoryPort.findById(subtema.id()).isEmpty());
        assertTrue(verdaderoOFalsoRepositoryPort.findById(preguntaId).isEmpty());
    }
}
