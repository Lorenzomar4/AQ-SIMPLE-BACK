package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PreguntaSimpleFullDTO;

public interface ObtenerPreguntaFullUseCase {

    PreguntaSimpleFullDTO obtenerFull(Long id);
}
