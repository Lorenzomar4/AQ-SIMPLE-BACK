package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TemaPostDTO {

    Long id;

    String name;

    public List<PreguntaPostDTO> questionList = new ArrayList<>();;


    public TemaPostDTO(Long id, String name, List<PreguntaPostDTO> questionList) {
        this.id = id;
        this.name = name;
        this.questionList = questionList;
    }
}
