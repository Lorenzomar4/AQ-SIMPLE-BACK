package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearCuestionarioUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearCuestionarioService implements CrearCuestionarioUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public CrearCuestionarioService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional
    public TemarioBasicDTO crear(TemarioBasicDTO temarioBasicDTO) {
        Temario temario = new Temario();
        temario.setTitulo(temarioBasicDTO.name());
        temario.setTipo(TipoAResponder.CUESTIONARIO);

        LocalDateTime ahora = LocalDateTime.now();
        temario.setFechaDeCreacion(ahora);
        temario.setUltimaActualizacion(ahora);

        Temario guardado = temarioRepositoryPort.save(temario);

        return new TemarioBasicDTO(guardado.getId(), guardado.getTitulo(), guardado.getFechaDeCreacion(), null);
    }
}
