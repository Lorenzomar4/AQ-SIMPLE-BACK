package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;

public interface EditarDesplegableIndependienteUseCase {

    DesplegableIndependiente editar(PostPreguntaDTO postPreguntaDTO);
}
