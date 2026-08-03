package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarOpcionMultipleService implements EditarOpcionMultipleUseCase {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public EditarOpcionMultipleService(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Override
    @Transactional
    public OpcionMultiple editar(PostPreguntaDTO postPreguntaDTO) {
        OpcionMultiple opcionMultiple = opcionMultipleRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        opcionMultiple.setTitulo(postPreguntaDTO.titulo());
        opcionMultiple.setDescripcion(postPreguntaDTO.descripcion());
        opcionMultiple.setListaDeOpciones(postPreguntaDTO.listaDeOpcionesConSuRespuestaReal());

        return opcionMultipleRepositoryPort.save(opcionMultiple);
    }
}
