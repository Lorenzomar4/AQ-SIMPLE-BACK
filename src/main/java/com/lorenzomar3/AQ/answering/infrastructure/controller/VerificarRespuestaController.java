package com.lorenzomar3.AQ.answering.infrastructure.controller;

import com.lorenzomar3.AQ.Service.PreguntaService;
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
import com.lorenzomar3.AQ.model.TipoAResponder;
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

import java.util.List;

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
    private final PreguntaService preguntaService;

    @Autowired
    public VerificarRespuestaController(VerificarRespuestaPreguntaSimpleHandler verificarRespuestaPreguntaSimpleHandler,
                                         VerificarRespuestaVerdaderoOFalsoHandler verificarRespuestaVerdaderoOFalsoHandler,
                                         VerificarRespuestaSeleccionUnicaHandler verificarRespuestaSeleccionUnicaHandler,
                                         VerificarRespuestaOpcionMultipleHandler verificarRespuestaOpcionMultipleHandler,
                                         VerificarRespuestaDesplegableCompartidoHandler verificarRespuestaDesplegableCompartidoHandler,
                                         VerificarRespuestaDesplegableIndependienteHandler verificarRespuestaDesplegableIndependienteHandler,
                                         PreguntaService preguntaService) {
        this.verificarRespuestaPreguntaSimpleHandler = verificarRespuestaPreguntaSimpleHandler;
        this.verificarRespuestaVerdaderoOFalsoHandler = verificarRespuestaVerdaderoOFalsoHandler;
        this.verificarRespuestaSeleccionUnicaHandler = verificarRespuestaSeleccionUnicaHandler;
        this.verificarRespuestaOpcionMultipleHandler = verificarRespuestaOpcionMultipleHandler;
        this.verificarRespuestaDesplegableCompartidoHandler = verificarRespuestaDesplegableCompartidoHandler;
        this.verificarRespuestaDesplegableIndependienteHandler = verificarRespuestaDesplegableIndependienteHandler;
        this.preguntaService = preguntaService;
    }

    @PostMapping("/questions/verify")
    public ResponseEntity<Boolean> verifyRequestForUser(@RequestBody RespuestaDePreguntaDTO respuestaDelUsuario) {
        logger.info("[POST /questions/verify] preguntaId={}, tipo={}", respuestaDelUsuario.idPregunta(), respuestaDelUsuario.tipoDePregunta());

        Boolean esCorrecta;
        if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.PREGUNTA_SIMPLE) {
            esCorrecta = verificarRespuestaPreguntaSimpleHandler.ejecutar(
                    new VerificarRespuestaPreguntaSimpleCommand(respuestaDelUsuario.idPregunta(), respuestaDelUsuario.respuestaBooleana()));
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.VERDADERO_FALSO) {
            esCorrecta = verificarRespuestaVerdaderoOFalsoHandler.ejecutar(
                    new VerificarRespuestaVerdaderoOFalsoCommand(respuestaDelUsuario.idPregunta(), respuestaDelUsuario.respuestaBooleana()));
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.SELECCION_UNICA) {
            List<OpcionRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpciones().stream()
                    .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                    .toList();
            esCorrecta = verificarRespuestaSeleccionUnicaHandler.ejecutar(
                    new VerificarRespuestaSeleccionUnicaCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.OPCION_MULTIPLE) {
            List<OpcionRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpciones().stream()
                    .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                    .toList();
            esCorrecta = verificarRespuestaOpcionMultipleHandler.ejecutar(
                    new VerificarRespuestaOpcionMultipleCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.DESPLEGABLE_COMPARTIDO) {
            List<OpcionDeDesplegableCompartidoRespuestaDTO> opcionesDelUsuario = respuestaDelUsuario.listaDeOpcionesParaDesplegableCompartidos().stream()
                    .map(opcion -> new OpcionDeDesplegableCompartidoRespuestaDTO(opcion.getId(), opcion.getRespuesta()))
                    .toList();
            esCorrecta = verificarRespuestaDesplegableCompartidoHandler.ejecutar(
                    new VerificarRespuestaDesplegableCompartidoCommand(respuestaDelUsuario.idPregunta(), opcionesDelUsuario));
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.DESPLEGABLE_INDEPENDIENTE) {
            List<SubPreguntaRespuestaDTO> subPreguntasDelUsuario = respuestaDelUsuario.listaDeSeleccionesUnicasParaDesplegableIndependiente().stream()
                    .map(subPregunta -> new SubPreguntaRespuestaDTO(subPregunta.getId(), subPregunta.getListaDeOpcionesDisponible().stream()
                            .map(opcion -> new OpcionRespuestaDTO(opcion.getId(), opcion.getLaRespuestaEs()))
                            .toList()))
                    .toList();
            esCorrecta = verificarRespuestaDesplegableIndependienteHandler.ejecutar(
                    new VerificarRespuestaDesplegableIndependienteCommand(respuestaDelUsuario.idPregunta(), subPreguntasDelUsuario));
        } else {
            esCorrecta = preguntaService.verifyResponse(respuestaDelUsuario);
        }

        return new ResponseEntity<>(esCorrecta, HttpStatus.OK);
    }
}
