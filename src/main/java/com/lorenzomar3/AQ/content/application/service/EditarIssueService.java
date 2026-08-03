package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.TemarioBasicDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EditarIssueService implements EditarIssueUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public EditarIssueService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional
    public TemarioBasicDTO editar(TemarioBasicDTO temarioBasicDTO) {
        Temario temario = temarioRepositoryPort.findById(temarioBasicDTO.id())
                .orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        temario.setTitulo(temarioBasicDTO.name());
        temario.setUltimaActualizacion(LocalDateTime.now());

        Temario actualizado = temarioRepositoryPort.save(temario);

        return new TemarioBasicDTO(actualizado.getId(), actualizado.getTitulo(), actualizado.getFechaDeCreacion(), null);
    }
}
