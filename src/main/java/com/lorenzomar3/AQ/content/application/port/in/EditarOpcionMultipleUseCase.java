package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface EditarOpcionMultipleUseCase {

    OpcionMultiple editar(PostPreguntaDTO postPreguntaDTO);
}
