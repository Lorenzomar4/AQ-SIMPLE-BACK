package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarPreguntaSimpleService implements EditarPreguntaUseCase {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public EditarPreguntaSimpleService(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional
    public PreguntaSimple editar(PostPreguntaDTO postPreguntaDTO) {
        PreguntaSimple preguntaSimple = preguntaSimpleRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        preguntaSimple.setTitulo(postPreguntaDTO.titulo());
        preguntaSimple.setDescripcion(postPreguntaDTO.descripcion());
        preguntaSimple.setRespuestaEstablecida(postPreguntaDTO.respuestaEstablecida());

        return preguntaSimpleRepositoryPort.save(preguntaSimple);
    }
}
