package com.lorenzomar3.AQ.answering.infrastructure.controller;

import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.answering.application.port.in.VerificarRespuestaPreguntaSimpleUseCase;
import com.lorenzomar3.AQ.answering.application.port.in.VerificarRespuestaVerdaderoOFalsoUseCase;
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

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class VerificarRespuestaController {

    private static final Logger logger = LoggerFactory.getLogger(VerificarRespuestaController.class);

    private final VerificarRespuestaPreguntaSimpleUseCase verificarRespuestaPreguntaSimpleUseCase;
    private final VerificarRespuestaVerdaderoOFalsoUseCase verificarRespuestaVerdaderoOFalsoUseCase;
    private final PreguntaService preguntaService;

    @Autowired
    public VerificarRespuestaController(VerificarRespuestaPreguntaSimpleUseCase verificarRespuestaPreguntaSimpleUseCase,
                                         VerificarRespuestaVerdaderoOFalsoUseCase verificarRespuestaVerdaderoOFalsoUseCase,
                                         PreguntaService preguntaService) {
        this.verificarRespuestaPreguntaSimpleUseCase = verificarRespuestaPreguntaSimpleUseCase;
        this.verificarRespuestaVerdaderoOFalsoUseCase = verificarRespuestaVerdaderoOFalsoUseCase;
        this.preguntaService = preguntaService;
    }

    @PostMapping("/questions/verify")
    public ResponseEntity<Boolean> verifyRequestForUser(@RequestBody RespuestaDePreguntaDTO respuestaDelUsuario) {
        logger.info("[POST /questions/verify] preguntaId={}, tipo={}", respuestaDelUsuario.idPregunta(), respuestaDelUsuario.tipoDePregunta());

        Boolean esCorrecta;
        if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.PREGUNTA_SIMPLE) {
            esCorrecta = verificarRespuestaPreguntaSimpleUseCase.verificar(respuestaDelUsuario);
        } else if (respuestaDelUsuario.tipoDePregunta() == TipoAResponder.VERDADERO_FALSO) {
            esCorrecta = verificarRespuestaVerdaderoOFalsoUseCase.verificar(respuestaDelUsuario);
        } else {
            esCorrecta = preguntaService.verifyResponse(respuestaDelUsuario);
        }

        return new ResponseEntity<>(esCorrecta, HttpStatus.OK);
    }
}
