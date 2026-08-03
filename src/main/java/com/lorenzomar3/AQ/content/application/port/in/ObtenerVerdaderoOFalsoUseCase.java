package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.VerdaderoOFalsoFetchDTO;

public interface ObtenerVerdaderoOFalsoUseCase {

    VerdaderoOFalsoFetchDTO obtener(Long id);
}
