package com.lorenzomar3.AQ.Controller;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsAleatoriosDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsCriticosUseCase;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = {"*"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class ResponderController {

    private static final Logger logger = LoggerFactory.getLogger(ResponderController.class);

    ObtenerIdsAleatoriosDePreguntasUseCase obtenerIdsAleatoriosDePreguntasUseCase;

    ObtenerIdsCriticosUseCase obtenerIdsCriticosUseCase;

    @Autowired
    public ResponderController(ObtenerIdsAleatoriosDePreguntasUseCase obtenerIdsAleatoriosDePreguntasUseCase,
                                ObtenerIdsCriticosUseCase obtenerIdsCriticosUseCase) {
        this.obtenerIdsAleatoriosDePreguntasUseCase = obtenerIdsAleatoriosDePreguntasUseCase;
        this.obtenerIdsCriticosUseCase = obtenerIdsCriticosUseCase;
    }

    //Beta
    @PostMapping("/questions/random-ids")
    public List<Long> obtenerListaDeIdsDePreguntas(@RequestBody ObtenerPreguntaDTO obtenerPreguntaDTO) {
        logger.info("[POST /questions/random-ids] id={}, tipo={}", obtenerPreguntaDTO.id(), obtenerPreguntaDTO.tipoAResponder());

        return obtenerIdsAleatoriosDePreguntasUseCase.obtenerIds(obtenerPreguntaDTO);
    }

    @GetMapping("/questions/{id}/critical-ids")
    public List<Long> obtenerIdsCriticos(@PathVariable Long id) {
        logger.info("[GET /questions/{}/critical-ids]", id);
        return obtenerIdsCriticosUseCase.obtenerIdsCriticos(id);
    }


}
