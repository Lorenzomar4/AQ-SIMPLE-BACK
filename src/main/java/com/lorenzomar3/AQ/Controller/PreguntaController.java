package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EditarPreguntaUseCase;
import com.lorenzomar3.AQ.dto.newDto.*;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.PreguntaSimple;
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

        CreateQuestionResponseDTO createQuestionResponseDTO = getQuestionDTO.tipo() == TipoAResponder.PREGUNTA_SIMPLE
                ? crearPreguntaUseCase.crear(getQuestionDTO)
                : preguntaService.createaQuestion(getQuestionDTO);

        return new ResponseEntity<>(createQuestionResponseDTO, HttpStatus.OK);
    }





    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("[DELETE /questions/{}]", id);
        preguntaService.delete(id);
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
