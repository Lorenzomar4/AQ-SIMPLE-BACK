package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.TemarioBasicDTO;

public interface CrearCuestionarioUseCase {

    TemarioBasicDTO crear(TemarioBasicDTO temarioBasicDTO);
}
