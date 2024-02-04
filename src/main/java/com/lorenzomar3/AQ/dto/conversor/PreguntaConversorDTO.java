package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.PreguntaPostDTO;
import com.lorenzomar3.AQ.model.Pregunta;

import java.util.Objects;

public class PreguntaConversorDTO {


    public static Pregunta fromJSON(PreguntaPostDTO preguntaPostDTO) {

        Long id = preguntaPostDTO.getId();

        Pregunta pregunta =new Pregunta(preguntaPostDTO.getQuestion(), preguntaPostDTO.getAnswer(), preguntaPostDTO.getStringList());

        if(!Objects.isNull(id)){
            pregunta.setId(id);
        }


        return new Pregunta(preguntaPostDTO.getQuestion(), preguntaPostDTO.getAnswer(), preguntaPostDTO.getStringList());

    }

    public static PreguntaPostDTO toDTO(Pregunta pregunta) {
        System.out.println("previo error");
        return new PreguntaPostDTO( pregunta.getId(),pregunta.getPregunta(), pregunta.getRespuestaTexto(), pregunta.getListaDePalabras());
    }
}
