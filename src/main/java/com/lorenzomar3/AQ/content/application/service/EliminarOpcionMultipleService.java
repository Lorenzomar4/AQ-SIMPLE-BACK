package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarOpcionMultipleService implements EliminarOpcionMultipleUseCase {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public EliminarOpcionMultipleService(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        opcionMultipleRepositoryPort.deleteById(id);
    }
}
