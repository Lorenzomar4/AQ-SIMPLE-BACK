package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PreguntaPostDTO {

    public Long id;

    public String question;

    public String answer;

    public Boolean critico;

    public List<String> stringList = new ArrayList<>();;

    public PreguntaPostDTO(Long id, String question, String answer, List<String> stringList, Boolean critico) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.stringList = stringList;
        this.critico =critico;
    }
}
