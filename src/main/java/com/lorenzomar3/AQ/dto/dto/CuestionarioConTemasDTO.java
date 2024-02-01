package com.lorenzomar3.AQ.dto.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CuestionarioConTemasDTO {

    public CuestionarioSimpleDTO questionnaire;

    public List<TemaSinPreguntasDTO> issueList;


    public CuestionarioConTemasDTO(CuestionarioSimpleDTO questionnaire, List<TemaSinPreguntasDTO> issue) {
        this.questionnaire = questionnaire;
        this.issueList = issue;
    }

}
