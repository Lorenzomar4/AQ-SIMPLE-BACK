package com.lorenzomar3.AQ.Service;


import com.lorenzomar3.AQ.Repository.CuestionarioRepository;
import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.dto.dto.TemaSinPreguntasDTO;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TemaService {

    @Autowired
    TemaRepository temaRepository;

    @Autowired
    CuestionarioService cuestionarioService;

    @Autowired
    CuestionarioRepository cuestionarioRepository;



    public List<Tema> getAllPreguntas() {
        return null;
    }


    public Tema getById(Long id){
        Tema tema = temaRepository.findById(id).orElseThrow( () -> new ErrorDeNegocio("no se encontro el tema"));
        return tema;
    }

    @Transactional
    public void saveIssue(TemaSinPreguntasDTO temaSinPreguntasDTO){



        Cuestionario cuestionario =  cuestionarioService.getById(temaSinPreguntasDTO.getIdquestionnaire());
        Tema  tema= new Tema();

        if(temaSinPreguntasDTO.getId() != null){
            tema.setId(temaSinPreguntasDTO.getId());
        }


        tema.setNombreDeTema(temaSinPreguntasDTO.getName());
        cuestionario.agregarTema(tema);
        cuestionarioRepository.save(cuestionario);
        System.out.println("x2");




    }


}
