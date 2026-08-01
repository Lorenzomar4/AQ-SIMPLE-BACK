package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.PreguntaSimpleFullDTO;

public interface ObtenerPreguntaFullUseCase {

    PreguntaSimpleFullDTO obtenerFull(Long id);
}
