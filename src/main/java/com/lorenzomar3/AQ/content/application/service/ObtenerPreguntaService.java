package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PreguntaSimpleFetchDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerPreguntaService implements ObtenerPreguntaUseCase {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public ObtenerPreguntaService(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PreguntaSimpleFetchDTO obtener(Long id) {
        PreguntaSimple preguntaSimple = preguntaSimpleRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new PreguntaSimpleFetchDTO(preguntaSimple.getId(), preguntaSimple.getTitulo(),
                preguntaSimple.getDescripcion(), preguntaSimple.getIdDuenio(), preguntaSimple.getFechaDeCreacion(),
                preguntaSimple.getUltimaActualizacion(), preguntaSimple.getTipo(),
                preguntaSimple.getIntentosParaQueDejeDeSerCriticoDisponible(), preguntaSimple.getImagenTitulo(),
                preguntaSimple.getRespuestaPrecisa());
    }
}
