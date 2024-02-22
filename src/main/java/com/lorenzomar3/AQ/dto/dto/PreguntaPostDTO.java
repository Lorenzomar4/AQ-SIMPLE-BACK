package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter

public class PreguntaPostDTO {

    public Long id;

    public Long idQuestionnaire;

    public String question;

    public String answer;

    public Boolean critico;

    public String questionimage;

    public String answerimage;

    public List<String> stringList = new ArrayList<>();;

    public PreguntaPostDTO(Long id, String question, String answer, Boolean critico, String questionimage, String answerimage, List<String> stringList) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.critico = critico;
        this.questionimage = questionimage;
        this.answerimage = answerimage;
        this.stringList = stringList;
    }

    public PreguntaPostDTO() {
    }
}
