package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.PreguntaPostDTO;
import com.lorenzomar3.AQ.model.Pregunta;

public class PreguntaConversorDTO {


    public static Pregunta fromJSON(PreguntaPostDTO preguntaPostDTO){

        return new Pregunta(preguntaPostDTO.getQuestion(),preguntaPostDTO.getAnswer(),preguntaPostDTO.getStringList());

    }

    public static PreguntaPostDTO toDTO(Pregunta pregunta){
        return new PreguntaPostDTO(pregunta.getPregunta() , pregunta.getRespuestaTexto(), pregunta.getListaDePreguntas());
    }
}
