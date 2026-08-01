package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.PreguntaSimpleFetchDTO;

public interface ObtenerPreguntaUseCase {

    PreguntaSimpleFetchDTO obtener(Long id);
}
