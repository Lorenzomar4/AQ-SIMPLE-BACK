package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TemaPostDTO {

    Long id;

    String name;

    public TemaPostDTO(String name, List<PreguntaPostDTO> preguntaPostDTOList) {
        this.name = name;
        this.preguntaPostDTOList = preguntaPostDTOList;
    }

    public List<PreguntaPostDTO> preguntaPostDTOList = new ArrayList<>();;



}
