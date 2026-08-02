package com.lorenzomar3.AQ.answering.infrastructure.controller;

import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaPreguntaSimpleCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaPreguntaSimpleHandler;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaVerdaderoOFalsoCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaVerdaderoOFalsoHandler;
import com.lorenzomar3.AQ.answering.application.command.OpcionRespuestaDTO;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaSeleccionUnicaCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaSeleccionUnicaHandler;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaOpcionMultipleCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaOpcionMultipleHandler;
import com.lorenzomar3.AQ.answering.application.command.OpcionDeDesplegableCompartidoRespuestaDTO;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaDesplegableCompartidoCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaDesplegableCompartidoHandler;
import com.lorenzomar3.AQ.answering.application.command.SubPreguntaRespuestaDTO;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaDesplegableIndependienteCommand;
import com.lorenzomar3.AQ.answering.application.command.VerificarRespuestaDesplegableIndependienteHandler;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class VerificarRespuestaController {

    private static final Logger logger = LoggerFactory.getLogger(VerificarRespuestaController.class);

    private final VerificarRespuestaPreguntaSimpleHandler verificarRespuestaPreguntaSimpleHandler;
    private final VerificarRespuestaVerdaderoOFalsoHandler verificarRespuestaVerdaderoOFalsoHandler;
    private final VerificarRespuestaSeleccionUnicaHandler verificarRespuestaSeleccionUnicaHandler;
    private final VerificarRespuestaOpcionMultipleHandler verificarRespuestaOpcionMultipleHandler;
    private final VerificarRespuestaDesplegableCompartidoHandler verificarRespuestaDesplegableCompartidoHandler;
    private final VerificarRespuestaDesplegableIndependienteHandler verificarRespuestaDesplegableIndependienteHandler;

    private final Map<TipoAResponder, Function<RespuestaDePreguntaDTO, Boolean>> mapDeVerificacion = new HashMap<>();

    @Autowired
    public VerificarRespuestaController(VerificarRespuestaPreguntaSimpleHandler verificarRespuestaPreguntaSimpleHandler,
                                         VerificarRespuestaVerdaderoOFalsoHandler verificarRespuestaVerdaderoOFalsoHandler,
                                         VerificarRespuestaSeleccionUnicaHandler verificarRespuestaSeleccionUnicaHandler,
                                         VerificarRespuestaOpcionMultipleHandler verificarRespuestaOpcionMultipleHandler,
                                         VerificarRespuestaDesplegableCompartidoHandler verificarRespuestaDesplegableCompartidoHandler,
                                         VerificarRespuestaDesplegableIndependienteHandler verificarRespuestaDesplegableIndependienteHandler) {
        this.verificarRespuestaPreguntaSimpleHandler = verificarRespuestaPreguntaSimpleHandler;
        this.verificarRespuestaVerdaderoOFalsoHandler = verificarRespuestaVerdaderoOFalsoHandler;
        this.verificarRespuestaSeleccionUnicaHandler = verificarRespuestaSeleccionUnicaHandler;
        this.verificarRespuestaOpcionMultipleHandler = verificarRespuestaOpcionMultipleHandler;
        this.verificarRespuestaDesplegableCompartidoHandler = verificarRespuestaDesplegableCompartidoHandler;
        this.verificarRespuestaDesplegableIndependienteHandler = verificarRespuestaDesplegableIndependienteHandler;
    }

    @PostConstruct
    private void init() {
        mapDeVerificacion.put(TipoAResponder.PREGUNTA_SIMPLE, this::verificarPreguntaSimple);
        mapDeVerificacion.put(TipoAResponder.VERDADERO_FALSO, this::verificarVerdaderoOFalso);
        mapDeVerificacion.put(TipoAResponder.SELECCION_UNICA, this::verificarSeleccionUnica);
        mapDeVerificacion.put(TipoAResponder.OPCION_MULTIPLE, this::verificarOpcionMultiple);
        mapDeVerificacion.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, this::verificarDesplegableCompartido);
        mapDeVerificacion.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, this::verificarDesplegableIndependiente);
    }

    @PostMapping("/questions/verify")
    public ResponseEntity<Boolean> verifyRequestForUser(@RequestBody RespuestaDePreguntaDTO respuestaDelUsuario) {
        logger.info("[POST /questions/verify] preguntaId={}, tipo={}", respuestaDelUsuario.idPregunta(), respuestaDelUsuario.tipoDePregunta());

        Function<RespuestaDePreguntaDTO, Boolean> verificar = mapDeVerificacion.get(respuestaDelUsuario.tipoDePregunta());
        if (verificar == null) {
            throw new BussinesException("Error, el tipo de pregunta solicitado no está soportado");
        }

        return new ResponseEntity<>(verificar.apply(respuestaDelUsuario), HttpStatus.OK);
    }

    private Boolean verificarPreguntaSimple(RespuestaDePreguntaDTO respuestaDelUsuario) {
        return verificarRespuestaPreguntaSimpleHandler.ejecutar(
                new VerificarRespuestaPreguntaSimpleCommand(respuestaDelUsuario.idPregunta(), respuestaDelUsuario.respuestaBooleana()));
    }

    private Boolean verificarVerdaderoOFalso(RespuestaDePreguntaDTO respuestaDelUsuario) {
        return verificarRespuestaVerdaderoOFalsoHandler.ejecutar(
                new VerificarRespuestaVerdaderoOFalsoCommand(respuestaDelUsuario.idPregunta(), respuestaDelUsuario.respuestaBooleana()));
    }

    private Boolean verificarSeleccionUnica(RespuestaDePreguntaDTO respuestaDelUsuario) {
        List<OpcionRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpciones().stream()
                .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                .toList();
        return verificarRespuestaSeleccionUnicaHandler.ejecutar(
                new VerificarRespuestaSeleccionUnicaCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
    }

    private Boolean verificarOpcionMultiple(RespuestaDePreguntaDTO respuestaDelUsuario) {
        List<OpcionRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpciones().stream()
                .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                .toList();
        return verificarRespuestaOpcionMultipleHandler.ejecutar(
                new VerificarRespuestaOpcionMultipleCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
    }

    private Boolean verificarDesplegableCompartido(RespuestaDePreguntaDTO respuestaDelUsuario) {
        List<OpcionDeDesplegableCompartidoRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpcionesParaDesplegableCompartidos().stream()
                .map(opcion -> new OpcionDeDesplegableCompartidoRespuestaDTO(opcion.getId(), opcion.getRespuesta()))
                .toList();
        return verificarRespuestaDesplegableCompartidoHandler.ejecutar(
                new VerificarRespuestaDesplegableCompartidoCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
    }

    private Boolean verificarDesplegableIndependiente(RespuestaDePreguntaDTO respuestaDelUsuario) {
        List<SubPreguntaRespuestaDTO> subPreguntasDelUsuario = respuestaDelUsuario.listaDeSeleccionesUnicasParaDesplegableIndependiente().stream()
                .map(subPregunta -> new SubPreguntaRespuestaDTO(subPregunta.getId(), subPregunta.getListaDeOpcionesDisponible().stream()
                        .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                        .toList()))
                .toList();
        return verificarRespuestaDesplegableIndependienteHandler.ejecutar(
                new VerificarRespuestaDesplegableIndependienteCommand(respuestaDelUsuario.idPregunta(), subPreguntasDelUsuario));
    }
}
