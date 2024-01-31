package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.TemaService;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TemaController {

    TemaService temaService;

    @GetMapping("/temas")
    public ResponseEntity<List<Tema>  > obtenerListaDePreguntas(){
        List<Tema> listaTema = temaService.getAllPreguntas();
        return new ResponseEntity<>(listaTema, HttpStatus.OK);
    }



}
