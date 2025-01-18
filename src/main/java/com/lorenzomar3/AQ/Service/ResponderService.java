package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResponderService {

    TemarioService temarioService;

    PreguntaService preguntaService;

    @Autowired
    public ResponderService(TemarioService temarioService, PreguntaService preguntaService) {
        this.temarioService = temarioService;
        this.preguntaService = preguntaService;
    }


    public List<Long> obtenerIdsDePreguntasDeManeraAleatoria(ObtenerPreguntaDTO obtenerPreguntaDTO) {

        List<TipoAResponder> listaTemarios = List.of(TipoAResponder.CUESTIONARIO, TipoAResponder.TEMA, TipoAResponder.SUBTEMA);

        if (listaTemarios.contains(obtenerPreguntaDTO.getTipoAResponder())) {
            return temarioService.obtenerTodosLosIdsDePreguntas(obtenerPreguntaDTO.getId());
        }


        return preguntaService.obtenerTodosLosIdsDePreguntas(obtenerPreguntaDTO);
    }


}
