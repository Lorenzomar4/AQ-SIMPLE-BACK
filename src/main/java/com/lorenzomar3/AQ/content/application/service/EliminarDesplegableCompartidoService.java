package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarDesplegableCompartidoService implements EliminarDesplegableCompartidoUseCase {

    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public EliminarDesplegableCompartidoService(DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        desplegableCompartidoRepositoryPort.deleteById(id);
    }
}
