package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarVerdaderoOFalsoService implements EliminarVerdaderoOFalsoUseCase {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public EliminarVerdaderoOFalsoService(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        verdaderoOFalsoRepositoryPort.deleteById(id);
    }
}
