package com.lorenzomar3.AQ.dto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreguntaAResponderDTO {
    public Long idTema ;

    public Long idCuestionario;

    public Boolean esCritico;

    public List<PreguntaPostDTO> questionnaireList = new ArrayList<>();

    public static PreguntaAResponderDTO singleQuestion(Long idTema, List<PreguntaPostDTO> questionnaireList ){
        PreguntaAResponderDTO p = new PreguntaAResponderDTO();

        List<PreguntaPostDTO> thequestionnaireList = questionnaireList;
        Collections.shuffle(thequestionnaireList);
        p.setIdTema(idTema);
        p.setQuestionnaireList(thequestionnaireList);
        return p;
    }


    public static PreguntaAResponderDTO fullQuestionList(Long idQuestionario, List<PreguntaPostDTO> questionnaireList ){
        PreguntaAResponderDTO p = new PreguntaAResponderDTO();

        List<PreguntaPostDTO> thequestionnaireList = questionnaireList;
        Collections.shuffle(thequestionnaireList);

        p.setIdCuestionario(idQuestionario);
        p.setQuestionnaireList(thequestionnaireList);
        return p;
    }


    public void setIdTema(Long idTema) {
        this.idTema = idTema;
    }

    public void setIdCuestionario(Long idCuestionario) {
        this.idCuestionario = idCuestionario;
    }

    public void setQuestionnaireList(List<PreguntaPostDTO> questionnaireList) {
        this.questionnaireList = questionnaireList;
    }
}
