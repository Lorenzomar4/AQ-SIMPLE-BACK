package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface CrearOpcionMultipleUseCase {

    CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO);
}
