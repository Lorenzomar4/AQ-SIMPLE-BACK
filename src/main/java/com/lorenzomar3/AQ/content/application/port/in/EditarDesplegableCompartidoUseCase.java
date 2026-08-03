package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;

public interface EditarDesplegableCompartidoUseCase {

    DesplegableCompartido editar(PostPreguntaDTO postPreguntaDTO);
}
