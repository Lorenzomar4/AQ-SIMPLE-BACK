package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.time.LocalDateTime;
import java.util.List;

public record TemaDTO(Long id, String name, LocalDateTime creationDate, LocalDateTime lastUpdateDate, TipoAResponder type, List<AResponderItemListDTO> itemList) {}