package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.ResponderService;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsAleatoriosDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsCriticosUseCase;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ResponderParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepositoryViejo;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private TemarioService temarioServiceViejo;

    @Autowired
    private ResponderService responderServiceViejo;

    @Autowired
    private CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    private CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    private CrearSeleccionUnicaUseCase crearSeleccionUnicaUseCase;

    @Autowired
    private CrearOpcionMultipleUseCase crearOpcionMultipleUseCase;

    @Autowired
    private CrearDesplegableCompartidoUseCase crearDesplegableCompartidoUseCase;

    @Autowired
    private CrearDesplegableIndependienteUseCase crearDesplegableIndependienteUseCase;

    @Autowired
    private ObtenerIdsAleatoriosDePreguntasUseCase obtenerIdsAleatoriosDePreguntasUseCase;

    @Autowired
    private ObtenerIdsCriticosUseCase obtenerIdsCriticosUseCase;

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

    private void marcarComoCritica(Long preguntaId) {
        var pregunta = preguntaRepositoryViejo.findById(preguntaId).orElseThrow();
        pregunta.setIntentosParaQueDejeDeSerCriticoDisponible(3);
        preguntaRepositoryViejo.save(pregunta);
    }

    @Test
    void randomIdsCasoContenedorDevuelveLosMismosIdsQueElFlujoViejoConSubtemasAnidadosYTiposMixtos() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario random-ids"));
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

        ObtenerPreguntaDTO dto = new ObtenerPreguntaDTO(cuestionarioId, TipoAResponder.CUESTIONARIO);

        List<Long> idsViejo = responderServiceViejo.obtenerIdsDePreguntasDeManeraAleatoria(dto);
        List<Long> idsNuevo = obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(dto);

        Set<Long> idsEsperados = Set.of(preguntaRaizId, preguntaTemaId, preguntaSubtemaId);

        assertEquals(idsEsperados, Set.copyOf(idsViejo));
        assertEquals(idsEsperados, Set.copyOf(idsNuevo));
        assertEquals(idsViejo.size(), idsNuevo.size());
    }

    @Test
    void randomIdsCasoHojaDevuelveElPropioIdIgualQueElFlujoViejoParaLosSeisTipos() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario random-ids hoja"));
        Long cuestionarioId = cuestionario.getId();
        cuestionariosCreados.add(cuestionarioId);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Pregunta simple", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                cuestionarioId, null, "Respuesta", null, null, null, null));
        Long idPreguntaSimple = obtenerUltimoHijo(cuestionarioId);

        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Verdadero o falso", "Desc", TipoAResponder.VERDADERO_FALSO,
                cuestionarioId, true, null, null, null, null, null));
        Long idVerdaderoOFalso = obtenerUltimoHijo(cuestionarioId);

        List<Opcion> opciones = List.of(
                new Opcion("River Plate", false),
                new Opcion("Boca Juniors", true),
                new Opcion("Independiente", false)
        );
        crearSeleccionUnicaUseCase.crear(new PostPreguntaDTO(null, "Seleccion unica", "Desc", TipoAResponder.SELECCION_UNICA,
                cuestionarioId, null, null, null, opciones, null, null));
        Long idSeleccionUnica = obtenerUltimoHijo(cuestionarioId);

        crearOpcionMultipleUseCase.crear(new PostPreguntaDTO(null, "Opcion multiple", "Desc", TipoAResponder.OPCION_MULTIPLE,
                cuestionarioId, null, null, null, opciones, null, null));
        Long idOpcionMultiple = obtenerUltimoHijo(cuestionarioId);

        List<OpcionDeDesplegableCompartido> opcionesDesplegableCompartido = List.of(
                new OpcionDeDesplegableCompartido("River Plate es un club de", "futbol"),
                new OpcionDeDesplegableCompartido("Boca Juniors es un club de", "futbol")
        );
        crearDesplegableCompartidoUseCase.crear(new PostPreguntaDTO(null, "Desplegable compartido", "Desc",
                TipoAResponder.DESPLEGABLE_COMPARTIDO, cuestionarioId, null, null, null, null,
                opcionesDesplegableCompartido, null));
        Long idDesplegableCompartido = obtenerUltimoHijo(cuestionarioId);

        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntas = List.of(
                new SeleccionUnicaParaDesplegableIndependiente("Los mamiferos son", List.of(
                        new Opcion("Vertebrados", true),
                        new Opcion("Invertebrados", false)
                ))
        );
        crearDesplegableIndependienteUseCase.crear(new PostPreguntaDTO(null, "Desplegable independiente", "Desc",
                TipoAResponder.DESPLEGABLE_INDEPENDIENTE, cuestionarioId, null, null, null, null, null, subPreguntas));
        Long idDesplegableIndependiente = obtenerUltimoHijo(cuestionarioId);

        List<ObtenerPreguntaDTO> dtosPorTipo = List.of(
                new ObtenerPreguntaDTO(idPreguntaSimple, TipoAResponder.PREGUNTA_SIMPLE),
                new ObtenerPreguntaDTO(idVerdaderoOFalso, TipoAResponder.VERDADERO_FALSO),
                new ObtenerPreguntaDTO(idSeleccionUnica, TipoAResponder.SELECCION_UNICA),
                new ObtenerPreguntaDTO(idOpcionMultiple, TipoAResponder.OPCION_MULTIPLE),
                new ObtenerPreguntaDTO(idDesplegableCompartido, TipoAResponder.DESPLEGABLE_COMPARTIDO),
                new ObtenerPreguntaDTO(idDesplegableIndependiente, TipoAResponder.DESPLEGABLE_INDEPENDIENTE)
        );

        for (ObtenerPreguntaDTO dto : dtosPorTipo) {
            List<Long> idsViejo = responderServiceViejo.obtenerIdsDePreguntasDeManeraAleatoria(dto);
            List<Long> idsNuevo = obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(dto);

            assertEquals(List.of(dto.id()), idsViejo);
            assertEquals(List.of(dto.id()), idsNuevo);
        }
    }

    @Test
    void randomIdsCasoContenedorConIdInexistenteLanzaBussinesExceptionEnAmbosCaminos() {
        ObtenerPreguntaDTO dto = new ObtenerPreguntaDTO(ID_INEXISTENTE, TipoAResponder.CUESTIONARIO);

        assertThrows(BussinesException.class, () -> responderServiceViejo.obtenerIdsDePreguntasDeManeraAleatoria(dto));
        assertThrows(BussinesException.class, () -> obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(dto));
    }

    @Test
    void randomIdsCasoHojaConIdInexistenteLanzaBussinesExceptionEnAmbosCaminosParaLosSeisTipos() {
        List<TipoAResponder> tiposHoja = List.of(
                TipoAResponder.PREGUNTA_SIMPLE,
                TipoAResponder.VERDADERO_FALSO,
                TipoAResponder.SELECCION_UNICA,
                TipoAResponder.OPCION_MULTIPLE,
                TipoAResponder.DESPLEGABLE_COMPARTIDO,
                TipoAResponder.DESPLEGABLE_INDEPENDIENTE
        );

        for (TipoAResponder tipo : tiposHoja) {
            ObtenerPreguntaDTO dto = new ObtenerPreguntaDTO(ID_INEXISTENTE, tipo);

            assertThrows(BussinesException.class, () -> responderServiceViejo.obtenerIdsDePreguntasDeManeraAleatoria(dto));
            assertThrows(BussinesException.class, () -> obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(dto));
        }
    }

    @Test
    void criticalIdsDevuelveLosMismosIdsQueElFlujoViejoEnUnArbolDeMasDeDosNivelesConCriticosYNoCriticosMezclados() {
        Temario cuestionario = temarioServiceViejo.saveTemarioCuestionario(new Temario("Cuestionario critical-ids"));
        Long cuestionarioId = cuestionario.getId();
        cuestionariosCreados.add(cuestionarioId);

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Tema", null, cuestionarioId));
        Long temaId = obtenerUltimoHijo(cuestionarioId);

        temarioServiceViejo.crearNuevoTemarioHijo(new TemarioBasicDTO(null, "Subtema", null, temaId));
        Long subtemaId = obtenerUltimoHijo(temaId);

        // Nivel 1 (cuestionario): una critica, una no critica.
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Raiz critica", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                cuestionarioId, null, "Respuesta", null, null, null, null));
        Long raizCritica = obtenerUltimoHijo(cuestionarioId);
        marcarComoCritica(raizCritica);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Raiz no critica", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                cuestionarioId, null, "Respuesta", null, null, null, null));
        Long raizNoCritica = obtenerUltimoHijo(cuestionarioId);

        // Nivel 2 (tema): una critica, una no critica.
        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Tema critica", "Desc", TipoAResponder.VERDADERO_FALSO,
                temaId, true, null, null, null, null, null));
        Long temaCritica = obtenerUltimoHijo(temaId);
        marcarComoCritica(temaCritica);

        crearVerdaderoOFalsoUseCase.crear(new PostPreguntaDTO(null, "Tema no critica", "Desc", TipoAResponder.VERDADERO_FALSO,
                temaId, true, null, null, null, null, null));
        Long temaNoCritica = obtenerUltimoHijo(temaId);

        // Nivel 3 (subtema): una critica, una no critica.
        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Subtema critica", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                subtemaId, null, "Respuesta", null, null, null, null));
        Long subtemaCritica = obtenerUltimoHijo(subtemaId);
        marcarComoCritica(subtemaCritica);

        crearPreguntaUseCase.crear(new PostPreguntaDTO(null, "Subtema no critica", "Desc", TipoAResponder.PREGUNTA_SIMPLE,
                subtemaId, null, "Respuesta", null, null, null, null));
        Long subtemaNoCritica = obtenerUltimoHijo(subtemaId);

        List<Long> idsViejo = responderServiceViejo.obtenerCriticosDeManeraAleatoria(cuestionarioId);
        List<Long> idsNuevo = obtenerIdsCriticosUseCase.obtenerIdsCriticos(cuestionarioId);

        Set<Long> idsEsperados = Set.of(raizCritica, temaCritica, subtemaCritica);

        assertEquals(idsEsperados, Set.copyOf(idsViejo));
        assertEquals(idsEsperados, Set.copyOf(idsNuevo));
        assertEquals(idsViejo.size(), idsNuevo.size());

        // Las no criticas quedan explicitamente afuera, en ambos caminos.
        assertFalse(idsViejo.contains(raizNoCritica) || idsViejo.contains(temaNoCritica) || idsViejo.contains(subtemaNoCritica));
        assertFalse(idsNuevo.contains(raizNoCritica) || idsNuevo.contains(temaNoCritica) || idsNuevo.contains(subtemaNoCritica));
    }

    @Test
    void criticalIdsConIdInexistenteDevuelveListaVaciaSinExcepcionEnAmbosCaminos() {
        List<Long> idsViejo = responderServiceViejo.obtenerCriticosDeManeraAleatoria(ID_INEXISTENTE);
        List<Long> idsNuevo = obtenerIdsCriticosUseCase.obtenerIdsCriticos(ID_INEXISTENTE);

        assertEquals(List.of(), idsViejo);
        assertEquals(List.of(), idsNuevo);
    }
}
