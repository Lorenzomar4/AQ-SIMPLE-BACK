package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.dto.dto.TemaSinPreguntasDTO;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;

import java.util.List;

public class TemaConversorDTO {

    public static TemaSinPreguntasDTO toSimpleDTO(Tema tema) {

        return new TemaSinPreguntasDTO(tema.getId(), tema.getNombreDeTema());

    }


    public static Tema fromJson(TemaSinPreguntasDTO tema) {
        return new Tema(tema.getName());

    }


    public static Tema fromJSONPost(TemaPostDTO temaPostDTO) {
        List<Pregunta> preguntaList = temaPostDTO
                .getPreguntaPostDTOList()
                .stream()
                .map(preg ->
                {
                    return new Pregunta(preg.getQuestion(), preg.getAnswer(), preg.getStringList());
                }).toList();
        return new Tema(temaPostDTO.getName(), preguntaList);

    }
}
