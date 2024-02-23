package com.lorenzomar3.AQ.dto.newDto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CuestionarioWithListDTO  extends  CuestionarioDTO{

    public List<AResponderItemListDTO> itemList;

    public CuestionarioWithListDTO(Long id, String name, LocalDateTime birthdate, List<AResponderItemListDTO> itemList) {
        super(id, name, birthdate);
        this.itemList = itemList;
    }

}
