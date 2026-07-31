package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;

public interface EditarVerdaderoOFalsoUseCase {

    VerdaderoOFalso editar(PostPreguntaDTO postPreguntaDTO);
}
