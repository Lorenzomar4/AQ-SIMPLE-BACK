package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.Service.PreguntaService;
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
import com.lorenzomar3.AQ.dto.newDto.*;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {
    private static final Logger logger = LoggerFactory.getLogger(PreguntaController.class);

    @Autowired
    PreguntaService preguntaService;

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


    @JsonView(View.JustToAnswer.class)
    @PostMapping("/questions/fetch")
    public ResponseEntity<Pregunta> getQuestion(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions/fetch] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipoAResponder());
        Pregunta pregunta = preguntaService.obtenerPregunta(getQuestionDTO.id(), getQuestionDTO.tipoAResponder());
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }

    @JsonView(View.Full.class)
    @PostMapping("/questions/fetch-full")
    @Transactional
    public ResponseEntity<Pregunta> getQuestionFull(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions/fetch-full] id={}, tipo={}", getQuestionDTO.id(), getQuestionDTO.tipoAResponder());

        Pregunta pregunta = preguntaService.obtenerPreguntaFull(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);
    }


    @PostMapping("/questions")
    public ResponseEntity<CreateQuestionResponseDTO> createQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        logger.info("[POST /questions] tipo={}, temarioId={}", getQuestionDTO.tipo(), getQuestionDTO.idTemarioPerteneciente());

        CreateQuestionResponseDTO createQuestionResponseDTO;
        if (getQuestionDTO.tipo() == TipoAResponder.PREGUNTA_SIMPLE) {
            createQuestionResponseDTO = crearPreguntaUseCase.crear(getQuestionDTO);
        } else if (getQuestionDTO.tipo() == TipoAResponder.VERDADERO_FALSO) {
            createQuestionResponseDTO = crearVerdaderoOFalsoUseCase.crear(getQuestionDTO);
        } else if (getQuestionDTO.tipo() == TipoAResponder.SELECCION_UNICA) {
            createQuestionResponseDTO = crearSeleccionUnicaUseCase.crear(getQuestionDTO);
        } else if (getQuestionDTO.tipo() == TipoAResponder.OPCION_MULTIPLE) {
            createQuestionResponseDTO = crearOpcionMultipleUseCase.crear(getQuestionDTO);
        } else if (getQuestionDTO.tipo() == TipoAResponder.DESPLEGABLE_COMPARTIDO) {
            createQuestionResponseDTO = crearDesplegableCompartidoUseCase.crear(getQuestionDTO);
        } else if (getQuestionDTO.tipo() == TipoAResponder.DESPLEGABLE_INDEPENDIENTE) {
            createQuestionResponseDTO = crearDesplegableIndependienteUseCase.crear(getQuestionDTO);
        } else {
            createQuestionResponseDTO = preguntaService.createaQuestion(getQuestionDTO);
        }

        return new ResponseEntity<>(createQuestionResponseDTO, HttpStatus.OK);
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

        if (getQuestionDTO.tipo() == TipoAResponder.PREGUNTA_SIMPLE) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        if (getQuestionDTO.tipo() == TipoAResponder.VERDADERO_FALSO) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        if (getQuestionDTO.tipo() == TipoAResponder.SELECCION_UNICA) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        if (getQuestionDTO.tipo() == TipoAResponder.OPCION_MULTIPLE) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        if (getQuestionDTO.tipo() == TipoAResponder.DESPLEGABLE_COMPARTIDO) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        if (getQuestionDTO.tipo() == TipoAResponder.DESPLEGABLE_INDEPENDIENTE) {
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

            return new ResponseEntity<>(respuesta, HttpStatus.OK);
        }

        Pregunta pregunta = preguntaService.updateQuestion(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }

    @PostMapping("/questions/verify")
    public ResponseEntity<Boolean> verifyRequestForUser(@RequestBody RespuestaDePreguntaDTO respuestaDelusuario) {
        logger.info("[POST /questions/verify] preguntaId={}, tipo={}", respuestaDelusuario.idPregunta(), respuestaDelusuario.tipoDePregunta());
        return new ResponseEntity<>(preguntaService.verifyResponse(respuestaDelusuario), HttpStatus.OK);
    }


    @PostMapping("/questions/inverse")
    public ResponseEntity<Void> createInverseQuestion(@RequestBody InverseQuestionCreateDTO inverseQuestionCreateDTO) {
        logger.info("[POST /questions/inverse] preguntaId={}, tipo={}", inverseQuestionCreateDTO.idQuestion(), inverseQuestionCreateDTO.tipo());
        preguntaService.createInverseQuestion(inverseQuestionCreateDTO);



        return new ResponseEntity<>(HttpStatus.OK);

    }

}
