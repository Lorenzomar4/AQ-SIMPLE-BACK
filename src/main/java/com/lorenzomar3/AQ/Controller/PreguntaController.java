package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Service.CuestionarioService;
import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.Service.TemaService;
import com.lorenzomar3.AQ.dto.conversor.PreguntaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaAResponderDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaPostDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaSolicitudDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class PreguntaController {

    @Autowired
    PreguntaService preguntaService;

    @Autowired
    TemaService temaService;

    @Autowired
    CuestionarioService cuestionarioService;

    @PostMapping("/preguntas")
    public ResponseEntity<PreguntaAResponderDTO> obtenerPreguntas(@RequestBody PreguntaSolicitudDTO preguntaSolicitudDTO) {


        if (preguntaSolicitudDTO.getSolicitud().equals("singleQuestion")) {

            Pregunta pregunta = preguntaService.getPreguntaById(preguntaSolicitudDTO.getId());

            PreguntaAResponderDTO preguntaAResponderDTO =
                    PreguntaAResponderDTO.singleQuestion(preguntaSolicitudDTO.getId(), List.of(PreguntaConversorDTO.toDTO(pregunta)));
            return new ResponseEntity<>(preguntaAResponderDTO, HttpStatus.OK);

        } else if (preguntaSolicitudDTO.getSolicitud().equals("forissue")) {

            System.out.println("forissue");


            Tema tema = temaService.getById(preguntaSolicitudDTO.getId());
            List<Pregunta> listaDePreguntas1 = tema.getListaDePreguntas();
            List<PreguntaPostDTO> listaDePreguntasDTO = listaDePreguntas1
                    .stream().map(pregunt -> PreguntaConversorDTO.toDTO(pregunt)).collect(Collectors.toList());


            PreguntaAResponderDTO preguntaAResponderDTOb =
                    PreguntaAResponderDTO.singleQuestion(preguntaSolicitudDTO.getId(), listaDePreguntasDTO);

            return new ResponseEntity<>(preguntaAResponderDTOb, HttpStatus.OK);

        } else {

            Cuestionario cuestionario = cuestionarioService.obtenerTodo(preguntaSolicitudDTO.getId());
            List<Pregunta> preguntass = cuestionario.todasLasPreguntasQueTieneElCuestionario();
            List<PreguntaPostDTO> listaDePreguntasDTOb = preguntass.stream().map(preg -> PreguntaConversorDTO.toDTO(preg)).collect(Collectors.toList());

            Collections.shuffle(listaDePreguntasDTOb);

            PreguntaAResponderDTO preguntaAResponderDTOc =
                    PreguntaAResponderDTO.singleQuestion(preguntaSolicitudDTO.getId(), listaDePreguntasDTOb);


            return new ResponseEntity<>(preguntaAResponderDTOc, HttpStatus.OK);

        }
    }

    @PutMapping("/bienRespondido/{id}")
    public ResponseEntity bienRespondido(@PathVariable Long id) {

        System.out.println("bienxxx");


        preguntaService.bienRespondido(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }


    @PutMapping("/malRespondido/{id}")
    public ResponseEntity <Void>  malRespondido(@PathVariable Long id) {

        System.out.println("noxxx");

        preguntaService.malRespondido(id);

        return new ResponseEntity<>(HttpStatus.OK);

    }




}
