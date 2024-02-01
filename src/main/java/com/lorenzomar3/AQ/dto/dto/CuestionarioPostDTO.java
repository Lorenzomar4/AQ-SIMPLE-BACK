package com.lorenzomar3.AQ.dto.dto;


import lombok.Getter;

import java.util.List;

@Getter
public class CuestionarioPostDTO {

    public CuestionarioSimpleDTO questionnaire;

    public List<TemaPostDTO> temaPostDTOList;

    public CuestionarioPostDTO(CuestionarioSimpleDTO questionnaire, List<TemaPostDTO> temaPostDTOList) {
        this.questionnaire = questionnaire;
        this.temaPostDTOList = temaPostDTOList;
    }
}
