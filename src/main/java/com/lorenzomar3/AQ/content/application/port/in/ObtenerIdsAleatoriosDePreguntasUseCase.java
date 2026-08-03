package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.ObtenerPreguntaDTO;

import java.util.List;

public interface ObtenerIdsAleatoriosDePreguntasUseCase {

    List<Long> obtenerIds(ObtenerPreguntaDTO dto);
}
