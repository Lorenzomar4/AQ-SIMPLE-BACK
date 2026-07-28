package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarPreguntaSimpleService implements EliminarPreguntaUseCase {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public EliminarPreguntaSimpleService(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        preguntaSimpleRepositoryPort.deleteById(id);
    }
}
