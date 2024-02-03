package com.lorenzomar3.AQ.dto.dto;

import lombok.Getter;

@Getter
public class CuestionarioSimpleDTO {

    public Long id;

    public String name;

    public CuestionarioSimpleDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
