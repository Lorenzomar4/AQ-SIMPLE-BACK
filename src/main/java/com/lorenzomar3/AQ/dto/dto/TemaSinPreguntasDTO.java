package com.lorenzomar3.AQ.dto.dto;

public class TemaSinPreguntasDTO {
    public Long id;
    public String name;

    public TemaSinPreguntasDTO(Long id, String nombreDeTema) {
        this.id = id;
        this.name = nombreDeTema;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
