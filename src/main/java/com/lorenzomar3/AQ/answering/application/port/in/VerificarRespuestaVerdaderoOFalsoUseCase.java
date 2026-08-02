package com.lorenzomar3.AQ.answering.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;

public interface VerificarRespuestaVerdaderoOFalsoUseCase {

    Boolean verificar(RespuestaDePreguntaDTO respuesta);
}
