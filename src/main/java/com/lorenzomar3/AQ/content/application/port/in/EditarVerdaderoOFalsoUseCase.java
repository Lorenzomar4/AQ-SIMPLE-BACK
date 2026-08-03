package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface EditarVerdaderoOFalsoUseCase {

    VerdaderoOFalso editar(PostPreguntaDTO postPreguntaDTO);
}
