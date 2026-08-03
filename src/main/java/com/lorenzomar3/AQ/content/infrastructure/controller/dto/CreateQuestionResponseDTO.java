package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

public record CreateQuestionResponseDTO(Long idFather, TipoAResponder questionType) {}