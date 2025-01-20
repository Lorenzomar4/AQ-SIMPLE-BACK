package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.Repository.AResponderRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.dto.newDto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ResponderService {

    TemarioService temarioService;

    PreguntaService preguntaService;

    PreguntaRepository preguntaRepository;

    AResponderRepository aResponderRepository;

    @Autowired
    public ResponderService(TemarioService temarioService, PreguntaService preguntaService ,  AResponderRepository aResponderRepository) {
        this.temarioService = temarioService;
        this.preguntaService = preguntaService;
        this.aResponderRepository = aResponderRepository;
    }


    public List<Long> obtenerIdsDePreguntasDeManeraAleatoria(ObtenerPreguntaDTO obtenerPreguntaDTO) {

        List<TipoAResponder> listaTemarios = List.of(TipoAResponder.CUESTIONARIO, TipoAResponder.TEMA, TipoAResponder.SUBTEMA);

        if (listaTemarios.contains(obtenerPreguntaDTO.getTipoAResponder())) {
            return temarioService.obtenerTodosLosIdsDePreguntas(obtenerPreguntaDTO.getId());
        }


        return preguntaService.obtenerTodosLosIdsDePreguntas(obtenerPreguntaDTO);
    }

    public ArrayList<Long> obtenerCriticosDeManeraAleatoria(Long id){
        ArrayList<Long> listIds =  aResponderRepository.getCriticsIdsForQuestion(id);

        Collections.shuffle(listIds);

        return  listIds;

    }


}
