package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

public record CreateQuestionResponseDTO(Long idFather, TipoAResponder questionType) {}