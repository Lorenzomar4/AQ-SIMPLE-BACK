package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.PreguntaRepository;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreguntaService {

    @Autowired
    public PreguntaRepository preguntaRepository;

    @Transactional(readOnly = true)
    public Pregunta getPreguntaById(Long id ){
        return preguntaRepository.findById(id).orElseThrow( ()-> new ErrorDeNegocio("no se encuentra la pregunta solicitada"));
    }


    @Transactional
    public void bienRespondido(Long id){
        Pregunta pregunta =   preguntaRepository.findById(id)
                .orElseThrow( ()-> new ErrorDeNegocio("no se encuentra la pregunta solicitada"));

        pregunta.descontarRepregunta();

        preguntaRepository.save(pregunta);

    }


    @Transactional
    public void malRespondido(Long id){
        Pregunta pregunta =   preguntaRepository.findById(id)
                .orElseThrow( ()-> new ErrorDeNegocio("no se encuentra la pregunta solicitada"));

        pregunta.equivocacion();

        preguntaRepository.save(pregunta);

    }
}
