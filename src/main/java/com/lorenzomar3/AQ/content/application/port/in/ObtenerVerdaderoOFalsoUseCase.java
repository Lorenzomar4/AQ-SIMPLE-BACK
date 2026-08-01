package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.VerdaderoOFalsoFetchDTO;

public interface ObtenerVerdaderoOFalsoUseCase {

    VerdaderoOFalsoFetchDTO obtener(Long id);
}
