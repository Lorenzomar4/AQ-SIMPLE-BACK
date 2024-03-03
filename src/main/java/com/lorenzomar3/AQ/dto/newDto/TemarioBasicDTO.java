package com.lorenzomar3.AQ.dto.newDto;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TemarioBasicDTO {

    public Long id;


    public String name;


    public LocalDateTime creationDate;

    //Puede llegar a ser nulo si estamos creando un Temario Padre, osea del tipo Cuestionario.
    Long fatherid;

    public TemarioBasicDTO(Long id, String titulo, LocalDateTime fechaDeCreacion) {
        this.id = id;
        this.name = titulo;
        this.creationDate = fechaDeCreacion;
    }
}
