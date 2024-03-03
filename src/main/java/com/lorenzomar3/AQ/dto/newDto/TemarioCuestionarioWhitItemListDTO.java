package com.lorenzomar3.AQ.dto.newDto;

import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
public class TemarioCuestionarioWhitItemListDTO extends TemarioBasicDTO {


    public List<AResponderItemListDTO> itemList;

    public TemarioCuestionarioWhitItemListDTO(Long id, String titulo, LocalDateTime fechaDeCreacion) {
        super(id, titulo, fechaDeCreacion);
    }
}
