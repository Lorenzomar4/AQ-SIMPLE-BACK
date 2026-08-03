package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

public record AResponderItemListDTO(Long id, String name, TipoAResponder type, Integer numberOfQuestions, Boolean isCritic) {}
