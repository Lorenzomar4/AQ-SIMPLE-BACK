package com.lorenzomar3.AQ.dto.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TemaSinPreguntasDTO {
    @Getter
    public Long id;
    @Getter
    public String name;
    @Getter
    public Long idquestionnaire;

    public TemaSinPreguntasDTO(Long id, String nombreDeTema) {
        this.id = id;
        this.name = nombreDeTema;
    }

    public TemaSinPreguntasDTO(Long id, String name, Long idquestionnaire) {
        this.id = id;
        this.name = name;
        this.idquestionnaire = idquestionnaire;
    }
}
