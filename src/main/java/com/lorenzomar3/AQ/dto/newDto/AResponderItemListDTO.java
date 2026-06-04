package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

public record AResponderItemListDTO(Long id, String name, TipoAResponder type, Integer numberOfQuestions, Boolean isCritic) {}
