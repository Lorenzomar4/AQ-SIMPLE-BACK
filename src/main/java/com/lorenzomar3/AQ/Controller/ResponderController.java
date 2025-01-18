package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.Service.PreguntaService;
import com.lorenzomar3.AQ.Service.ResponderService;
import com.lorenzomar3.AQ.Service.TemarioService;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class ResponderController {


    ResponderService responderService;

    @Autowired
    public ResponderController(ResponderService responderService) {
        this.responderService = responderService;
    }

    //Beta
    @PostMapping("/questionIdList")
    public List<Long> obtenerListaDeIdsDePreguntas(@RequestBody ObtenerPreguntaDTO obtenerPreguntaDTO) {

        List<Long> listaDeIds = responderService.obtenerIdsDePreguntasDeManeraAleatoria(obtenerPreguntaDTO);

        List<Long> listaMutable = new ArrayList<>(listaDeIds);

        Collections.shuffle(listaMutable);

        return listaMutable;

    }


}
