package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;

public interface CrearDesplegableIndependienteUseCase {

    CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO);
}
