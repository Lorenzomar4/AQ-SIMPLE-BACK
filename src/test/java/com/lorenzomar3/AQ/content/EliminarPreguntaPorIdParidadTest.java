package com.lorenzomar3.AQ.content;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.DesplegableCompartidoRepositorio;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.DesplegableIndependienteRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.OpcionMultipleRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaSimpleRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.SeleccionUnicaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaPorIdUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EliminarPreguntaPorIdParidadTest {

    private static final Long ID_INEXISTENTE = Long.MAX_VALUE;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private PreguntaSimpleRepository preguntaSimpleRepositoryViejo;

    @Autowired
    private SeleccionUnicaRepository seleccionUnicaRepositoryViejo;

    @Autowired
    private OpcionMultipleRepository opcionMultipleRepositoryViejo;

    @Autowired
    private DesplegableCompartidoRepositorio desplegableCompartidoRepositoryViejo;

    @Autowired
    private DesplegableIndependienteRepository desplegableIndependienteRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

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
    private EliminarPreguntaPorIdUseCase eliminarPreguntaPorIdUseCase;

    @Autowired
    private PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    @Autowired
    private VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    @Autowired
    private SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    @Autowired
    private OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    @Autowired
    private DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    @Autowired
    private DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba paridad EliminarPreguntaPorId", TipoAResponder.CUESTIONARIO);
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
    void eliminarPreguntaSimplePorIdProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.PREGUNTA_SIMPLE,
                temarioId, null, "Respuesta de prueba", null, null, null, null);

        crearPreguntaUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearPreguntaUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(preguntaSimpleRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(preguntaSimpleRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(preguntaSimpleRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(preguntaSimpleRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarVerdaderoOFalsoPorIdProduceElMismoResultadoQueElFlujoViejo() {
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.VERDADERO_FALSO,
                temarioId, true, null, null, null, null, null);

        crearVerdaderoOFalsoUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearVerdaderoOFalsoUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(preguntaRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(preguntaRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(verdaderoOFalsoRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(verdaderoOFalsoRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarSeleccionUnicaPorIdProduceElMismoResultadoQueElFlujoViejoIncluyendoLasOpciones() {
        List<Opcion> opciones = List.of(new Opcion("River Plate", false), new Opcion("Boca Juniors", true));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.SELECCION_UNICA,
                temarioId, null, null, null, opciones, null, null);

        crearSeleccionUnicaUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearSeleccionUnicaUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(seleccionUnicaRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(seleccionUnicaRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(seleccionUnicaRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(seleccionUnicaRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarOpcionMultiplePorIdProduceElMismoResultadoQueElFlujoViejoIncluyendoLasOpciones() {
        List<Opcion> opciones = List.of(new Opcion("River Plate", true), new Opcion("Boca Juniors", true), new Opcion("Independiente", false));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.OPCION_MULTIPLE,
                temarioId, null, null, null, opciones, null, null);

        crearOpcionMultipleUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearOpcionMultipleUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(opcionMultipleRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(opcionMultipleRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(opcionMultipleRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(opcionMultipleRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarDesplegableCompartidoPorIdProduceElMismoResultadoQueElFlujoViejoIncluyendoLasOpciones() {
        List<OpcionDeDesplegableCompartido> opciones = List.of(
                new OpcionDeDesplegableCompartido("River Plate es un club de", "futbol"),
                new OpcionDeDesplegableCompartido("Boca Juniors es un club de", "futbol"));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.DESPLEGABLE_COMPARTIDO,
                temarioId, null, null, null, null, opciones, null);

        crearDesplegableCompartidoUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableCompartidoUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(desplegableCompartidoRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(desplegableCompartidoRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(desplegableCompartidoRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(desplegableCompartidoRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarDesplegableIndependientePorIdProduceElMismoResultadoQueElFlujoViejoIncluyendoLasSubpreguntas() {
        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntas = List.of(
                new SeleccionUnicaParaDesplegableIndependiente("Los mamiferos son", List.of(
                        new Opcion("Vertebrados", true),
                        new Opcion("Invertebrados", false))),
                new SeleccionUnicaParaDesplegableIndependiente("Los peces son", List.of(
                        new Opcion("Acuaticos", true),
                        new Opcion("Terrestres", false))));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta a borrar", "Descripcion", TipoAResponder.DESPLEGABLE_INDEPENDIENTE,
                temarioId, null, null, null, null, null, subPreguntas);

        crearDesplegableIndependienteUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableIndependienteUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        preguntaServiceViejo.delete(idViejo);
        eliminarPreguntaPorIdUseCase.eliminar(idNuevo);

        assertTrue(desplegableIndependienteRepositoryViejo.findById(idViejo).isEmpty());
        assertTrue(desplegableIndependienteRepositoryViejo.findById(idNuevo).isEmpty());
        assertTrue(desplegableIndependienteRepositoryPort.findById(idViejo).isEmpty());
        assertTrue(desplegableIndependienteRepositoryPort.findById(idNuevo).isEmpty());
    }

    @Test
    void eliminarPreguntaPorIdConIdInexistenteLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> eliminarPreguntaPorIdUseCase.eliminar(ID_INEXISTENTE));
    }

    @Test
    void eliminarPreguntaPorIdConIdDeIssueLanzaBussinesException() {
        assertThrows(BussinesException.class, () -> eliminarPreguntaPorIdUseCase.eliminar(temarioId));
    }
}
