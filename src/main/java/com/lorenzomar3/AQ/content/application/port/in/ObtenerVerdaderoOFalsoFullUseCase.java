package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.VerdaderoOFalsoFullDTO;

public interface ObtenerVerdaderoOFalsoFullUseCase {

    VerdaderoOFalsoFullDTO obtenerFull(Long id);
}
