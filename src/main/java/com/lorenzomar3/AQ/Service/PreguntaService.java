package com.lorenzomar3.AQ.Service;

import com.lorenzomar3.AQ.JsonVisualizador;
import com.lorenzomar3.AQ.Repository.PreguntaRepository;
import com.lorenzomar3.AQ.Repository.TemaRepository;
import com.lorenzomar3.AQ.dto.conversor.PreguntaConversorDTO;
import com.lorenzomar3.AQ.dto.dto.PreguntaPostDTO;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreguntaService {

    @Autowired
    public PreguntaRepository preguntaRepository;

    @Autowired
    public TemaService temaService;

    @Autowired
    public TemaRepository temaRepository;

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
        //aver como quedo

        JsonVisualizador.verJson(pregunta);

    }


    @Transactional
    public void guardarPregunta(PreguntaPostDTO preguntaPostDTO){

        Tema tema=  temaService.getById(preguntaPostDTO.getIdQuestionnaire());

        Pregunta pregunta = PreguntaConversorDTO.fromJSON(preguntaPostDTO);

        tema.agregarPregunta(pregunta);

        temaRepository.save(tema);

    }
}
