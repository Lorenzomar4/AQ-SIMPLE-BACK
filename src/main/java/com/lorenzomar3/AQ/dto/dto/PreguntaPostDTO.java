package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;

import java.util.List;

@Data
public class PreguntaPostDTO {

    public Long id;

    public String question;

    public String answer;

    public List<String> stringList;



}
