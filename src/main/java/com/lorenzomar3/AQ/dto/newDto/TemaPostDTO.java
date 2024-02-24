package com.lorenzomar3.AQ.dto.newDto;

import lombok.Getter;

@Getter
public class TemaPostDTO {

    Long id;

    String name;

    public TemaPostDTO() {
    }

    Long fatherid;


    public TemaPostDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
