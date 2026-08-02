package com.lorenzomar3.AQ.answering;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemarioRepository;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica, a través del controller real (MockMvc), que SELECCION_UNICA, OPCION_MULTIPLE,
 * DESPLEGABLE_COMPARTIDO y DESPLEGABLE_INDEPENDIENTE devuelven en POST /questions/verify el
 * mismo resultado que el camino legacy (PreguntaService.verifyResponse) tras migrarlos a los
 * Command/Handler de answering/ (VerificarRespuestaSeleccionUnicaHandler y análogos). Cada test
 * compara el camino viejo invocado directamente (fixture A) contra el nuevo camino atravesando
 * el controller vía MockMvc (fixture B), sobre datos equivalentes.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VerificarRespuestaTiposNoMigradosNoRegresionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemarioRepository temarioRepository;

    @Autowired
    private PreguntaRepository preguntaRepositoryViejo;

    @Autowired
    private PreguntaService preguntaServiceViejo;

    @Autowired
    private CrearSeleccionUnicaUseCase crearSeleccionUnicaUseCase;

    @Autowired
    private CrearOpcionMultipleUseCase crearOpcionMultipleUseCase;

    @Autowired
    private CrearDesplegableCompartidoUseCase crearDesplegableCompartidoUseCase;

    @Autowired
    private CrearDesplegableIndependienteUseCase crearDesplegableIndependienteUseCase;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long temarioId;
    private final List<Long> preguntasCreadas = new ArrayList<>();

    @BeforeEach
    void crearTemarioDePrueba() {
        Temario temario = new Temario("Temario de prueba no-regresion verify", TipoAResponder.CUESTIONARIO);
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

    private void verificarPorAmbosCaminosDevuelveElMismoResultado(Long idViejo, RespuestaDePreguntaDTO respuestaViejo,
                                                                     Long idNuevo, RespuestaDePreguntaDTO respuestaNuevo) throws Exception {
        Boolean resultadoViejo = preguntaServiceViejo.verifyResponse(respuestaViejo);

        mockMvc.perform(post("/questions/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(respuestaNuevo)))
                .andExpect(status().isOk())
                .andExpect(content().string(resultadoViejo.toString()));
    }

    @Test
    void seleccionUnicaVerifySigueIgualQueAntes() throws Exception {
        List<Opcion> opciones = List.of(new Opcion("River Plate", false), new Opcion("Boca Juniors", true));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta seleccion unica", "Descripcion", TipoAResponder.SELECCION_UNICA,
                temarioId, null, null, null, opciones, null, null);

        crearSeleccionUnicaUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearSeleccionUnicaUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        SeleccionUnica entidadVieja = (SeleccionUnica) preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        SeleccionUnica entidadNueva = (SeleccionUnica) preguntaRepositoryViejo.findById(idNuevo).orElseThrow();

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.SELECCION_UNICA, null, null,
                entidadVieja.getListaDeOpcionesConSuRespuestaReal(), null, null);
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.SELECCION_UNICA, null, null,
                entidadNueva.getListaDeOpcionesConSuRespuestaReal(), null, null);

        verificarPorAmbosCaminosDevuelveElMismoResultado(idViejo, respuestaViejo, idNuevo, respuestaNuevo);
    }

    @Test
    void opcionMultipleVerifySigueIgualQueAntes() throws Exception {
        List<Opcion> opciones = List.of(new Opcion("River Plate", true), new Opcion("Boca Juniors", true), new Opcion("Independiente", false));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta opcion multiple", "Descripcion", TipoAResponder.OPCION_MULTIPLE,
                temarioId, null, null, null, opciones, null, null);

        crearOpcionMultipleUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearOpcionMultipleUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        OpcionMultiple entidadVieja = (OpcionMultiple) preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        OpcionMultiple entidadNueva = (OpcionMultiple) preguntaRepositoryViejo.findById(idNuevo).orElseThrow();

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.OPCION_MULTIPLE, null, null,
                entidadVieja.getListaDeOpcionesConSuRespuestaReal(), null, null);
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.OPCION_MULTIPLE, null, null,
                entidadNueva.getListaDeOpcionesConSuRespuestaReal(), null, null);

        verificarPorAmbosCaminosDevuelveElMismoResultado(idViejo, respuestaViejo, idNuevo, respuestaNuevo);
    }

    @Test
    void desplegableCompartidoVerifySigueIgualQueAntes() throws Exception {
        List<OpcionDeDesplegableCompartido> opciones = List.of(
                new OpcionDeDesplegableCompartido("River Plate es un club de", "futbol"),
                new OpcionDeDesplegableCompartido("Boca Juniors es un club de", "futbol"));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta desplegable compartido", "Descripcion", TipoAResponder.DESPLEGABLE_COMPARTIDO,
                temarioId, null, null, null, null, opciones, null);

        crearDesplegableCompartidoUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableCompartidoUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        DesplegableCompartido entidadVieja = (DesplegableCompartido) preguntaRepositoryViejo.findById(idViejo).orElseThrow();
        DesplegableCompartido entidadNueva = (DesplegableCompartido) preguntaRepositoryViejo.findById(idNuevo).orElseThrow();

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.DESPLEGABLE_COMPARTIDO, null, null,
                null, entidadVieja.getListaDeOpcionDesplegableCompartido(), null);
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.DESPLEGABLE_COMPARTIDO, null, null,
                null, entidadNueva.getListaDeOpcionDesplegableCompartido(), null);

        verificarPorAmbosCaminosDevuelveElMismoResultado(idViejo, respuestaViejo, idNuevo, respuestaNuevo);
    }

    @Test
    void desplegableIndependienteVerifySigueIgualQueAntes() throws Exception {
        List<SeleccionUnicaParaDesplegableIndependiente> subPreguntas = List.of(
                new SeleccionUnicaParaDesplegableIndependiente("Los mamiferos son", List.of(
                        new Opcion("Vertebrados", true),
                        new Opcion("Invertebrados", false))));
        PostPreguntaDTO dto = new PostPreguntaDTO(null, "Pregunta desplegable independiente", "Descripcion", TipoAResponder.DESPLEGABLE_INDEPENDIENTE,
                temarioId, null, null, null, null, null, subPreguntas);

        crearDesplegableIndependienteUseCase.crear(dto);
        Long idViejo = obtenerUltimoHijo();
        preguntasCreadas.add(idViejo);

        crearDesplegableIndependienteUseCase.crear(dto);
        Long idNuevo = obtenerUltimoHijo();
        preguntasCreadas.add(idNuevo);

        DesplegableIndependiente entidadVieja = obtenerDesplegableIndependienteConOpcionesInicializadas(idViejo);
        DesplegableIndependiente entidadNueva = obtenerDesplegableIndependienteConOpcionesInicializadas(idNuevo);

        RespuestaDePreguntaDTO respuestaViejo = new RespuestaDePreguntaDTO(idViejo, TipoAResponder.DESPLEGABLE_INDEPENDIENTE, null, null,
                null, null, entidadVieja.getListaDeOpcionDesplegableIndependiente());
        RespuestaDePreguntaDTO respuestaNuevo = new RespuestaDePreguntaDTO(idNuevo, TipoAResponder.DESPLEGABLE_INDEPENDIENTE, null, null,
                null, null, entidadNueva.getListaDeOpcionDesplegableIndependiente());

        verificarPorAmbosCaminosDevuelveElMismoResultado(idViejo, respuestaViejo, idNuevo, respuestaNuevo);
    }

    /**
     * A diferencia de los demas tipos, SeleccionUnicaParaDesplegableIndependiente tiene su propia
     * coleccion lazy anidada (listaDeOpcionesDisponible). preguntaRepositoryViejo.findById por si
     * solo cierra su transaccion apenas retorna, así que recorrerla despues (fuera de esta funcion)
     * dispara LazyInitializationException. Se fuerza la inicializacion acá, dentro de una
     * transaccion corta y dedicada solo a este fetch -sin tocar la transaccionalidad de los
     * altas/bajas del resto del test, que ya funcionan bien tal como estaban.
     */
    private DesplegableIndependiente obtenerDesplegableIndependienteConOpcionesInicializadas(Long id) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            DesplegableIndependiente entidad = (DesplegableIndependiente) preguntaRepositoryViejo.findById(id).orElseThrow();
            entidad.getListaDeOpcionDesplegableIndependiente().forEach(sub -> sub.getListaDeOpcionesDisponible().size());
            return entidad;
        });
    }
}
