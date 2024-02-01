package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class PreguntaPostDTO {

    public Long id;

    public String question;

    public String answer;

    public List<String> stringList;


    public PreguntaPostDTO(String question, String answer, List<String> stringList) {
        this.question = question;
        this.answer = answer;
        this.stringList = stringList;
    }
}
