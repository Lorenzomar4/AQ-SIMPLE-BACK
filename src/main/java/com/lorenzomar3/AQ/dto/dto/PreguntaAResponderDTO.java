package com.lorenzomar3.AQ.dto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreguntaAResponderDTO {
    public Long idTema ;

    public Long idCuestionario;

    public Boolean esCritico;

    public List<PreguntaPostDTO> questionnaireList = new ArrayList<>();

    public static PreguntaAResponderDTO singleQuestion(Long idTema, List<PreguntaPostDTO> questionnaireList ){
        PreguntaAResponderDTO p = new PreguntaAResponderDTO();
        p.setIdTema(idTema);
        p.setQuestionnaireList(questionnaireList);
        return p;
    }


    public static PreguntaAResponderDTO fullQuestionList(Long idQuestionario, List<PreguntaPostDTO> questionnaireList ){
        PreguntaAResponderDTO p = new PreguntaAResponderDTO();
        p.setIdCuestionario(idQuestionario);
        p.setQuestionnaireList(questionnaireList);
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
