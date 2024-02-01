package com.lorenzomar3.AQ.dto.dto;


import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class CuestionarioPostDTO {

    public CuestionarioSimpleDTO questionnaire;

    public List<TemaPostDTO> issueList = Collections.emptyList();
    public CuestionarioPostDTO(CuestionarioSimpleDTO questionnaire, List<TemaPostDTO> temaPostDTOList) {
        this.questionnaire = questionnaire;
        this.issueList = temaPostDTOList;
    }
}
