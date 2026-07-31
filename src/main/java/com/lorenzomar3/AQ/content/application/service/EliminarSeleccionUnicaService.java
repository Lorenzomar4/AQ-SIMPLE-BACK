package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarSeleccionUnicaService implements EliminarSeleccionUnicaUseCase {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public EliminarSeleccionUnicaService(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        seleccionUnicaRepositoryPort.deleteById(id);
    }
}
