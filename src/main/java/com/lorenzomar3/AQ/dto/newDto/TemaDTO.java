package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;
import java.util.List;

public class TemaDTO {

    public Long id;
    public String name;
    public LocalDateTime creationDate;
    public LocalDateTime  lastUpdateDate;
    public TipoAResponder type;
    public List<AResponderItemListDTO> itemList;

    public TemaDTO(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdateDate, TipoAResponder type, List<AResponderItemListDTO> itemList) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.type = type;
        this.itemList = itemList;
    }
}
