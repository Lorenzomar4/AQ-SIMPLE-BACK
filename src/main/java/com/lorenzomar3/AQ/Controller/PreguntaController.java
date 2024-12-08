package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
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
    @PostMapping("/getQuestionForAnswer")
    public ResponseEntity<Pregunta> getQuestion(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("/getQuestionForAnswer");
        Pregunta pregunta = preguntaService.obtenerPregunta(getQuestionDTO.getId(), getQuestionDTO.getTipoAResponder());
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }

    @JsonView(View.Full.class)
    @PostMapping("/getQuestionForAnswerFull")
    @Transactional
    public ResponseEntity<Pregunta> getQuestionFull(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        logger.info("/getQuestionForAnswerFull");

        Pregunta pregunta = preguntaService.obtenerPreguntaFull(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);
    }


    @JsonView(View.JustToAnswer.class)
    @PostMapping("/createQuestion")
    public ResponseEntity<CreateQuestionResponseDTO> createQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        logger.info("creacion de un cuestionario : endpoint /createQuestion ");

        CreateQuestionResponseDTO createQuestionResponseDTO = preguntaService.createaQuestion(getQuestionDTO);
        return new ResponseEntity<>(createQuestionResponseDTO, HttpStatus.OK);
    }


    @DeleteMapping("/questionDelete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logger.info("/questionDelete/"+id);
        preguntaService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @JsonView(View.JustToAnswer.class)
    @PutMapping("/editQuestion")
    public ResponseEntity<Pregunta> updateQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        logger.info("/editQuestion/");


        Pregunta pregunta = preguntaService.updateQuestion(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }

    @PostMapping("/verifyRequest")
    public ResponseEntity<Boolean> verifyRequestForUser(@RequestBody RespuestaDePreguntaDTO respuestaDelusuario) {
        logger.info("/verifyRequest/");
        return new ResponseEntity<>(preguntaService.verifyResponse(respuestaDelusuario), HttpStatus.OK);
    }




}
