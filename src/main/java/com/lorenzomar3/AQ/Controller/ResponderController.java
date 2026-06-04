package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.ResponderService;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class ResponderController {

    private static final Logger logger = LoggerFactory.getLogger(ResponderController.class);

    ResponderService responderService;

    @Autowired
    public ResponderController(ResponderService responderService) {
        this.responderService = responderService;
    }

    //Beta
    @PostMapping("/questions/random-ids")
    public List<Long> obtenerListaDeIdsDePreguntas(@RequestBody ObtenerPreguntaDTO obtenerPreguntaDTO) {
        logger.info("[POST /questions/random-ids] id={}, tipo={}", obtenerPreguntaDTO.id(), obtenerPreguntaDTO.tipoAResponder());

        List<Long> listaDeIds = responderService.obtenerIdsDePreguntasDeManeraAleatoria(obtenerPreguntaDTO);

        List<Long> listaMutable = new ArrayList<>(listaDeIds);

        Collections.shuffle(listaMutable);

        return listaMutable;

    }

    @GetMapping("/questions/{id}/critical-ids")
    public List<Long> obtenerIdsCriticos(@PathVariable Long id) {
        logger.info("[GET /questions/{}/critical-ids]", id);
        return responderService.obtenerCriticosDeManeraAleatoria(id);

    }


}
