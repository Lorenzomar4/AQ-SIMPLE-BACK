package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.Repository.PreguntaRepository;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PreguntaService {

    @Autowired
    PreguntaRepository preguntaRepository;

    @Transactional(readOnly = true)
    public List<Pregunta<?>> obtenerTodasLasPreguntas(){
        return preguntaRepository.findAll();
    }
}
