package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.PreguntaPostDTO;
import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.dto.dto.TemaSinPreguntasDTO;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TemaConversorDTO {

    public static TemaSinPreguntasDTO toSimpleDTO(Tema tema) {
        return new TemaSinPreguntasDTO(tema.getId(), tema.getNombreDeTema());
    }

    public static Tema fromJSONPost(TemaPostDTO temaPostDTO) {

        Long id = temaPostDTO.getId();

        List<Pregunta> preguntaList = temaPostDTO
                .getQuestionList()
                .stream()
                .map(preg ->
                {
                    return PreguntaConversorDTO.fromJSON(preg);
                }).toList();

        Tema  tema = new Tema(temaPostDTO.getName(), preguntaList);
        tema.setId(id); //borrar si causa errores



        return tema;

    }

    public static TemaPostDTO toDTOPost(Tema tema) {

        List<PreguntaPostDTO> preguntaPostDTOList = tema.getListaDePreguntas().stream()
                .map(preg -> {
                    return PreguntaConversorDTO.toDTO(preg);
                }).toList();

        return new TemaPostDTO(tema.getId() ,tema.getNombreDeTema(), preguntaPostDTOList);

    }
}
