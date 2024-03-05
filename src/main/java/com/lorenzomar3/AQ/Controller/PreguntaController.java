package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {

    @Autowired
    PreguntaService preguntaService;


    @JsonView(View.JustToAnswer.class)
    @PostMapping("/getQuestionForAnswer")
    public ResponseEntity<Pregunta> getQuestion(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {

        Pregunta pregunta = preguntaService.obtenerPregunta(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }


    @JsonView(View.Full.class)
    @PostMapping("/getQuestionForAnswerFull")
    @Transactional
    public ResponseEntity<Pregunta> getQuestionFull(@RequestBody ObtenerPreguntaDTO getQuestionDTO) {
        Pregunta pregunta = preguntaService.obtenerPreguntaFull(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }


    @JsonView(View.JustToAnswer.class)
    @PostMapping("/createQuestion")
    public ResponseEntity<AResponder> createQuestion(@RequestBody PostPreguntaDTO getQuestionDTO) {
        AResponder pregunta = preguntaService.createaQuestion(getQuestionDTO);
        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }


    @DeleteMapping("/questionDelete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        preguntaService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
