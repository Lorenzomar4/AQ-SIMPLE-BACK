package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.dto.newDto.*;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
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

        CreateQuestionResponseDTO createQuestionResponseDTO = preguntaService.createaQuestion(getQuestionDTO);
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
