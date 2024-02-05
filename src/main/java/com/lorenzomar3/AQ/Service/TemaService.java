package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    @Autowired
    TemaRepository temaRepository;



    public List<Tema> getAllPreguntas() {
        return null;
    }


    public Tema getById(Long id){
        Tema tema = temaRepository.findById(id).orElseThrow( () -> new ErrorDeNegocio("no se encontro el tema"));
        return tema;
    }
}
