package com.lorenzomar3.AQ.Controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.dto.dto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {

    @Autowired
    PreguntaService preguntaService;


    @PostMapping("/nuevaPregunta")
    public void crearPreguntaNueva() {

    }

    @JsonView(View.JustToAnswer.class)
    @PostMapping("/getQuestionForAnswer")
    public ResponseEntity<Pregunta> getQuestion(@RequestBody  ObtenerPreguntaDTO getQuestionDTO) {



        Pregunta pregunta = preguntaService.obtenerPregunta(getQuestionDTO);


        return new ResponseEntity<>(pregunta, HttpStatus.OK);

    }


}
