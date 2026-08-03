package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PreguntaSimpleFetchDTO;

public interface ObtenerPreguntaUseCase {

    PreguntaSimpleFetchDTO obtener(Long id);
}
