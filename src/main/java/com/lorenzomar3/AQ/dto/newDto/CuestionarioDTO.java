package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.Cuestionario;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class CuestionarioDTO {

    public Long id;

    public String name;

    public LocalDateTime creationDate;

    public CuestionarioDTO(Long id, String name, LocalDateTime creationDate) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
    }

}
