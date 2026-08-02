package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaPorIdUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaFullUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoFullUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.query.ObtenerSeleccionUnicaFullQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerSeleccionUnicaFullQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerSeleccionUnicaQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerSeleccionUnicaQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerOpcionMultipleFullQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerOpcionMultipleFullQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerOpcionMultipleQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerOpcionMultipleQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableCompartidoFullQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableCompartidoFullQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableCompartidoQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableCompartidoQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableIndependienteFullQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableIndependienteFullQueryHandler;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableIndependienteQuery;
import com.lorenzomar3.AQ.content.application.query.ObtenerDesplegableIndependienteQueryHandler;
import com.lorenzomar3.AQ.content.application.command.CrearPreguntaInversaCommand;
import com.lorenzomar3.AQ.content.application.command.CrearPreguntaInversaHandler;
import com.lorenzomar3.AQ.dto.newDto.*;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.VerdaderoOFalso;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.model.View;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {
    private static final Logger logger = LoggerFactory.getLogger(PreguntaController.class);

    @Autowired
    CrearPreguntaUseCase crearPreguntaUseCase;

    @Autowired
    EditarPreguntaUseCase editarPreguntaUseCase;

    @Autowired
    CrearVerdaderoOFalsoUseCase crearVerdaderoOFalsoUseCase;

    @Autowired
    EditarVerdaderoOFalsoUseCase editarVerdaderoOFalsoUseCase;

    @Autowired
    CrearSeleccionUnicaUseCase crearSeleccionUnicaUseCase;

    @Autowired
    EditarSeleccionUnicaUseCase editarSeleccionUnicaUseCase;

    @Autowired
    CrearOpcionMultipleUseCase crearOpcionMultipleUseCase;

    @Autowired
    EditarOpcionMultipleUseCase editarOpcionMultipleUseCase;

    @Autowired
    CrearDesplegableCompartidoUseCase crearDesplegableCompartidoUseCase;

    @Autowired
    EditarDesplegableCompartidoUseCase editarDesplegableCompartidoUseCase;

    @Autowired
    CrearDesplegableIndependienteUseCase crearDesplegableIndependienteUseCase;

    @Autowired
    EditarDesplegableIndependienteUseCase editarDesplegableIndependienteUseCase;

    @Autowired
    EliminarPreguntaPorIdUseCase eliminarPreguntaPorIdUseCase;

    @Autowired
    ObtenerPreguntaUseCase obtenerPreguntaUseCase;

    @Autowired
    ObtenerPreguntaFullUseCase obtenerPreguntaFullUseCase;

    @Autowired
    ObtenerVerdaderoOFalsoUseCase obtenerVerdaderoOFalsoUseCase;

    @Autowired
    ObtenerVerdaderoOFalsoFullUseCase obtenerVerdaderoOFalsoFullUseCase;

    @Autowired
    ObtenerSeleccionUnicaQueryHandler obtenerSeleccionUnicaQueryHandler;

    @Autowired
    ObtenerSeleccionUnicaFullQueryHandler obtenerSeleccionUnicaFullQueryHandler;

    @Autowired
    ObtenerOpcionMultipleQueryHandler obtenerOpcionMultipleQueryHandler;

    @Autowired
    ObtenerOpcionMultipleFullQueryHandler obtenerOpcionMultipleFullQueryHandler;

    @Autowired
    ObtenerDesplegableCompartidoQueryHandler obtenerDesplegableCompartidoQueryHandler;

    @Autowired
    ObtenerDesplegableCompartidoFullQueryHandler obtenerDesplegableCompartidoFullQueryHandler;

    @Autowired
    ObtenerDesplegableIndependienteQueryHandler obtenerDesplegableIndependienteQueryHandler;

    @Autowired
    ObtenerDesplegableIndependienteFullQueryHandler obtenerDesplegableIndependienteFullQueryHandler;

    @Autowired
    CrearPreguntaInversaHandler crearPreguntaInversaHandler;

    private final Map<TipoAResponder, Function<Long, Object>> mapDeObtencion = new HashMap<>();
    private final Map<TipoAResponder, Function<Long, Object>> mapDeObtencionFull = new HashMap<>();
    private final Map<TipoAResponder, Function<PostPreguntaDTO, CreateQuestionResponseDTO>> mapDeCreacion = new HashMap<>();
    private final Map<TipoAResponder, Function<PostPreguntaDTO, Pregunta>> mapDeEdicion = new HashMap<>();

    @PostConstruct
    private void init() {
        mapDeObtencion.put(TipoAResponder.PREGUNTA_SIMPLE, obtenerPreguntaUseCase::obtener);
        mapDeObtencion.put(TipoAResponder.VERDADERO_FALSO, obtenerVerdaderoOFalsoUseCase::obtener);
        mapDeObtencion.put(TipoAResponder.SELECCION_UNICA, id -> obtenerSeleccionUnicaQueryHandler.handle(new ObtenerSeleccionUnicaQuery(id)));
        mapDeObtencion.put(TipoAResponder.OPCION_MULTIPLE, id -> obtenerOpcionMultipleQueryHandler.handle(new ObtenerOpcionMultipleQuery(id)));
        mapDeObtencion.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, id -> obtenerDesplegableCompartidoQueryHandler.handle(new ObtenerDesplegableCompartidoQuery(id)));
        mapDeObtencion.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, id -> obtenerDesplegableIndependienteQueryHandler.handle(new ObtenerDesplegableIndependienteQuery(id)));

        mapDeObtencionFull.put(TipoAResponder.PREGUNTA_SIMPLE, obtenerPreguntaFullUseCase::obtenerFull);
        mapDeObtencionFull.put(TipoAResponder.VERDADERO_FALSO, obtenerVerdaderoOFalsoFullUseCase::obtenerFull);
        mapDeObtencionFull.put(TipoAResponder.SELECCION_UNICA, id -> obtenerSeleccionUnicaFullQueryHandler.handle(new ObtenerSeleccionUnicaFullQuery(id)));
        mapDeObtencionFull.put(TipoAResponder.OPCION_MULTIPLE, id -> obtenerOpcionMultipleFullQueryHandler.handle(new ObtenerOpcionMultipleFullQuery(id)));
        mapDeObtencionFull.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, id -> obtenerDesplegableCompartidoFullQueryHandler.handle(new ObtenerDesplegableCompartidoFullQuery(id)));
        mapDeObtencionFull.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, id -> obtenerDesplegableIndependienteFullQueryHandler.handle(new ObtenerDesplegableIndependienteFullQuery(id)));

        mapDeCreacion.put(TipoAResponder.PREGUNTA_SIMPLE, crearPreguntaUseCase::crear);
        mapDeCreacion.put(TipoAResponder.VERDADERO_FALSO, crearVerdaderoOFalsoUseCase::crear);
        mapDeCreacion.put(TipoAResponder.SELECCION_UNICA, crearSeleccionUnicaUseCase::crear);
        mapDeCreacion.put(TipoAResponder.OPCION_MULTIPLE, crearOpcionMultipleUseCase::crear);
        mapDeCreacion.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, crearDesplegableCompartidoUseCase::crear);
        mapDeCreacion.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, crearDesplegableIndependienteUseCase::crear);

        mapDeEdicion.put(TipoAResponder.PREGUNTA_SIMPLE, this::editarPreguntaSimple);
        mapDeEdicion.put(TipoAResponder.VERDADERO_FALSO, this::editarVerdaderoOFalso);
        mapDeEdicion.put(TipoAResponder.SELECCION_UNICA, this::editarSeleccionUnica);
        mapDeEdicion.put(TipoAResponder.OPCION_MULTIPLE, this::editarOpcionMultiple);
        mapDeEdicion.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, this::editarDesplegableCompartido);
        mapDeEdicion.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, this::editarDesplegableIndependiente);
    }


    @PostMapping("/questions/fetch")
    public ResponseEntity<Object> getQuestion(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions/fetch] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipoAResponder());

        Function<Long, Object> obtener = mapDeObtencion.get(getQuestionDTO.tipoAResponder());
        if (obtener == null) {
            throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
        }

        return new ResponseEntity<>(obtener.apply(getQuestionDTO.id()), HttpStatus.OK);
    }

    @PostMapping("/questions/fetch-full")
    @Transactional
    public ResponseEntity<Object> getQuestionFull(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions/fetch-full] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipoAResponder());

        Function<Long, Object> obtenerFull = mapDeObtencionFull.get(getQuestionDTO.tipoAResponder());
        if (obtenerFull == null) {
            throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
        }

        return new ResponseEntity<>(obtenerFull.apply(getQuestionDTO.id()), HttpStatus.OK);
    }


    @PostMapping("/questions")
    public ResponseEntity<CreateQuestionResponseDTO> createQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions] tipo={}, temarioId={}", getQuestionDTO.tipo(), getQuestionDTO.idTemarioPerteneciente());

        Function<PostPreguntaDTO, CreateQuestionResponseDTO> crear = mapDeCreacion.get(getQuestionDTO.tipo());
        if (crear == null) {
            throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
        }

        return new ResponseEntity<>(crear.apply(getQuestionDTO), HttpStatus.OK);
    }


    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("[DELETE /questions/{}]", id);
        eliminarPreguntaPorIdUseCase.eliminar(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @JsonView(View.JustToAnswer.class)
    @PutMapping("/questions")
    public ResponseEntity<Pregunta> updateQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        logger.info("[PUT /questions] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipo());

        Function<PostPreguntaDTO, Pregunta> editar = mapDeEdicion.get(getQuestionDTO.tipo());
        if (editar == null) {
            throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
        }

        return new ResponseEntity<>(editar.apply(getQuestionDTO), HttpStatus.OK);
    }

    @PostMapping("/questions/inverse")
    public ResponseEntity<Void> createInverseQuestion(@RequestBody InverseQuestionCreateDTO inverseQuestionCreateDTO) {
        logger.info("[POST /questions/inverse] preguntaId={}, tipo={}", inverseQuestionCreateDTO.idQuestion(), inverseQuestionCreateDTO.tipo());
        crearPreguntaInversaHandler.ejecutar(
                new CrearPreguntaInversaCommand(inverseQuestionCreateDTO.idQuestion(), inverseQuestionCreateDTO.tipo()));

        return new ResponseEntity<>(HttpStatus.OK);

    }

    private Pregunta editarPreguntaSimple(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.PreguntaSimple actualizada = editarPreguntaUseCase.editar(getQuestionDTO);

        PreguntaSimple respuesta = new PreguntaSimple();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.setRespuestaEstablecida(actualizada.getRespuestaEstablecida());
        respuesta.setRespuestaPrecisa(actualizada.getRespuestaPrecisa());

        return respuesta;
    }

    private Pregunta editarVerdaderoOFalso(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.VerdaderoOFalso actualizada = editarVerdaderoOFalsoUseCase.editar(getQuestionDTO);

        VerdaderoOFalso respuesta = new VerdaderoOFalso();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.respuestaVerdadera = actualizada.getRespuestaVerdadera();

        return respuesta;
    }

    private Pregunta editarSeleccionUnica(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.SeleccionUnica actualizada = editarSeleccionUnicaUseCase.editar(getQuestionDTO);

        SeleccionUnica respuesta = new SeleccionUnica();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.setListaDeOpcionesConSuRespuestaReal(
                actualizada.getListaDeOpciones().stream().map(opcion -> {
                    Opcion opcionVieja = new Opcion(opcion.getOpcion(), opcion.getLaRespuestaEs());
                    opcionVieja.setId(opcion.getId());
                    return opcionVieja;
                }).toList()
        );

        return respuesta;
    }

    private Pregunta editarOpcionMultiple(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.OpcionMultiple actualizada = editarOpcionMultipleUseCase.editar(getQuestionDTO);

        OpcionMultiple respuesta = new OpcionMultiple();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.setListaDeOpcionesConSuRespuestaReal(
                actualizada.getListaDeOpciones().stream().map(opcion -> {
                    Opcion opcionVieja = new Opcion(opcion.getOpcion(), opcion.getLaRespuestaEs());
                    opcionVieja.setId(opcion.getId());
                    return opcionVieja;
                }).toList()
        );

        return respuesta;
    }

    private Pregunta editarDesplegableCompartido(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.DesplegableCompartido actualizada = editarDesplegableCompartidoUseCase.editar(getQuestionDTO);

        DesplegableCompartido respuesta = new DesplegableCompartido();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.setListaDeOpcionDesplegableCompartido(
                actualizada.getListaDeOpciones().stream().map(opcion -> {
                    OpcionDeDesplegableCompartido opcionVieja = new OpcionDeDesplegableCompartido(opcion.getPregunta(), opcion.getRespuesta());
                    opcionVieja.setId(opcion.getId());
                    return opcionVieja;
                }).toList()
        );

        return respuesta;
    }

    private Pregunta editarDesplegableIndependiente(PostPreguntaDTO getQuestionDTO) {
        com.lorenzomar3.AQ.content.domain.DesplegableIndependiente actualizada = editarDesplegableIndependienteUseCase.editar(getQuestionDTO);

        DesplegableIndependiente respuesta = new DesplegableIndependiente();
        respuesta.setId(actualizada.getId());
        respuesta.setTitulo(actualizada.getTitulo());
        respuesta.setDescripcion(actualizada.getDescripcion());
        respuesta.setIdDuenio(actualizada.getIdDuenio());
        respuesta.setFechaDeCreacion(actualizada.getFechaDeCreacion());
        respuesta.setTipo(actualizada.getTipo());
        respuesta.setIntentosParaQueDejeDeSerCriticoDisponible(actualizada.getIntentosParaQueDejeDeSerCriticoDisponible());
        respuesta.setImagenTitulo(actualizada.getImagenTitulo());
        respuesta.setListaDeOpcionDesplegableIndependiente(
                actualizada.getListaDeOpciones().stream().map(subPregunta -> {
                    SeleccionUnicaParaDesplegableIndependiente subPreguntaVieja = new SeleccionUnicaParaDesplegableIndependiente(
                            subPregunta.getTitulo(),
                            subPregunta.getListaDeOpciones().stream().map(opcion -> {
                                Opcion opcionVieja = new Opcion(opcion.getOpcion(), opcion.getLaRespuestaEs());
                                opcionVieja.setId(opcion.getId());
                                return opcionVieja;
                            }).toList()
                    );
                    subPreguntaVieja.setId(subPregunta.getId());
                    return subPreguntaVieja;
                }).toList()
        );

        return respuesta;
    }

}
