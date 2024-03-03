package com.lorenzomar3.AQ.dto.newDto;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class TemarioCuestionarioCardDTO {

    public Long id;


    public String name;


    public LocalDateTime creationDate;

    public TemarioCuestionarioCardDTO(Long id, String titulo, LocalDateTime fechaDeCreacion) {
        this.id = id;
        this.name = titulo;
        this.creationDate = fechaDeCreacion;
    }
}
