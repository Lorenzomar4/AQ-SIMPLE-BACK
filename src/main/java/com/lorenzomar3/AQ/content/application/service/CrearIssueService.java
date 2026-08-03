package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.AResponderItemListDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.TemarioBasicDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearIssueService implements CrearIssueUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public CrearIssueService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional
    public AResponderItemListDTO crear(TemarioBasicDTO temarioBasicDTO) {
        Temario padre = temarioRepositoryPort.findById(temarioBasicDTO.fatherid())
                .orElseThrow(() -> new BussinesException("Error , no existe este cuestionario"));

        if (padre.getTipo() == TipoAResponder.SUBTEMA) {
            throw new BussinesException("No se puede agregar un subtema a otro subtema");
        }

        TipoAResponder tipoHijo = padre.getTipo() == TipoAResponder.CUESTIONARIO
                ? TipoAResponder.TEMA
                : TipoAResponder.SUBTEMA;

        Temario hijo = new Temario();
        hijo.setTitulo(temarioBasicDTO.name());
        hijo.setTipo(tipoHijo);
        hijo.setIdDuenio(padre.getId());

        LocalDateTime ahora = LocalDateTime.now();
        hijo.setFechaDeCreacion(ahora);
        hijo.setUltimaActualizacion(ahora);

        Temario hijoGuardado = temarioRepositoryPort.save(hijo);

        return new AResponderItemListDTO(hijoGuardado.getId(), hijoGuardado.getTitulo(), hijoGuardado.getTipo(), null, false);
    }
}
