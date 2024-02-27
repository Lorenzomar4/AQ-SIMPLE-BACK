package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {

    @Autowired
    PreguntaService preguntaService;



    @PostMapping("/nuevaPregunta")
    public void crearPreguntaNueva(){

    }






}
