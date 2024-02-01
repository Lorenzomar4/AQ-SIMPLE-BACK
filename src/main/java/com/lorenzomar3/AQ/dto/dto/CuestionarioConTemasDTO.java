package com.lorenzomar3.AQ.dto.dto;

import java.util.List;

public class CuestionarioConTemasDTO {

    public CuestionarioSimpleDTO questionnaire;

    public List<TemaSinPreguntasDTO> issueList;


    public CuestionarioConTemasDTO(CuestionarioSimpleDTO questionnaire, List<TemaSinPreguntasDTO> issue) {
        this.questionnaire = questionnaire;
        this.issueList = issue;
    }

    public CuestionarioSimpleDTO getQuestionnaire() {
        return questionnaire;
    }

    public List<TemaSinPreguntasDTO> getIssueList() {
        return issueList;
    }
}
