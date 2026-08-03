package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface EditarPreguntaUseCase {

    PreguntaSimple editar(PostPreguntaDTO postPreguntaDTO);
}
