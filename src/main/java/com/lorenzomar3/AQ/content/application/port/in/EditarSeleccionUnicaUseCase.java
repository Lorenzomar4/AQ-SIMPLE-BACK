package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface EditarSeleccionUnicaUseCase {

    SeleccionUnica editar(PostPreguntaDTO postPreguntaDTO);
}
