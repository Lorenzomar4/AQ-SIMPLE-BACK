package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.BasePreguntaRepositorio;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.OpcionMultipleRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.PreguntaRepository.SeleccionUnicaRepository;
import com.lorenzomar3.AQ.dto.dto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PreguntaService {

    public PreguntaService() {
    }

    @Autowired
    PreguntaRepository preguntaRepository;

    @Autowired
    SeleccionUnicaRepository seleccionUnicaRepository;

    @Autowired
    OpcionMultipleRepository opcionMultipleRepository;

    HashMap<TipoAResponder, BasePreguntaRepositorio<?>> mapDeRepositorios = new HashMap<>();

    @PostConstruct
    private void init(){
        mapDeRepositorios.put(TipoAResponder.PREGUNTA_SIMPLE, preguntaRepository);
        mapDeRepositorios.put(TipoAResponder.VERDADERO_FALSO, preguntaRepository);
        mapDeRepositorios.put(TipoAResponder.SELECCION_UNICA, seleccionUnicaRepository);
        mapDeRepositorios.put(TipoAResponder.OPCION_MULTIPLE, opcionMultipleRepository);

    }


    public Pregunta<?> obtenerPregunta(ObtenerPreguntaDTO getQuestionDTO) {

        return (Pregunta<?>) mapDeRepositorios.get(getQuestionDTO.getTipoAResponder()).findById(getQuestionDTO.getId()).orElseThrow(
                () -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));
    }


}
