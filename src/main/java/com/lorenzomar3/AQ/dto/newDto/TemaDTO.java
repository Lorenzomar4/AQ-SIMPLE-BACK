package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;
import java.util.List;

public record TemaDTO(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdateDate, TipoAResponder type, List<AResponderItemListDTO> itemList) {}