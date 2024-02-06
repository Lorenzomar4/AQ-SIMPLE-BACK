package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.dto.conversor.PreguntaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaAResponderDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaSolicitudDTO;
import com.lorenzomar3.AQ.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST , RequestMethod.DELETE , RequestMethod.PUT})
public class PreguntaController {

        @Autowired
        PreguntaService preguntaService;

        @PostMapping("/preguntas")
        public ResponseEntity<PreguntaAResponderDTO> obtenerPreguntas(@RequestBody PreguntaSolicitudDTO preguntaSolicitudDTO){


            if(preguntaSolicitudDTO.getSolicitud().equals("singleQuestion")){

                JsonVisualizador.verJson(preguntaSolicitudDTO);
                Pregunta pregunta = preguntaService.getPreguntaById(preguntaSolicitudDTO.getId());
                JsonVisualizador.verJson(pregunta);
                PreguntaAResponderDTO preguntaAResponderDTO =
                        PreguntaAResponderDTO.singleQuestion(preguntaSolicitudDTO.getId(), List.of(  PreguntaConversorDTO.toDTO(pregunta)));
                return new ResponseEntity<>(preguntaAResponderDTO, HttpStatus.OK);

            }

            return null;
        }

        @PutMapping ("/bienRespondido/{id}")
        public ResponseEntity bienRespondido(@PathVariable Long id){


            preguntaService.bienRespondido(id);

            return new ResponseEntity<>(HttpStatus.OK);

        }


    @PutMapping ("/malRespondido/{id}")
    public ResponseEntity malRespondido(@PathVariable Long id){

        preguntaService.malRespondido(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }


}
