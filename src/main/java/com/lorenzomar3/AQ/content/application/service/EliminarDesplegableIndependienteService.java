package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarDesplegableIndependienteService implements EliminarDesplegableIndependienteUseCase {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public EliminarDesplegableIndependienteService(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        desplegableIndependienteRepositoryPort.deleteById(id);
    }
}
