package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TemaPostDTO {

    Long id;

    String name;

    public TemaPostDTO() {
    }

    Long idquestionnaire;


    public TemaPostDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
