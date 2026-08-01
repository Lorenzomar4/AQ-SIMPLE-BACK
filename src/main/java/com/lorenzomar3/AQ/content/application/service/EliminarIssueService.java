package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EliminarIssueService implements EliminarIssueUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public EliminarIssueService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        temarioRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        temarioRepositoryPort.deleteById(id);
    }
}
