package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.TemaService;
import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST , RequestMethod.DELETE , RequestMethod.PUT})
public class TemaController {

    @Autowired

    TemaService temaService;

    @GetMapping("/temas")
    public ResponseEntity<List<Tema>  > obtenerListaDePreguntas(){
        List<Tema> listaTema = temaService.getAllPreguntas();
        return new ResponseEntity<>(listaTema, HttpStatus.OK);
    }

    @GetMapping("/tema/{id}")
    public ResponseEntity<TemaPostDTO> todoElTema(@PathVariable Long id) {
        Tema tema  = temaService.getById(id);
        TemaPostDTO temaDto = TemaConversorDTO.toDTOPost(tema);


        return new ResponseEntity<>(temaDto, HttpStatus.OK);
    }







}
