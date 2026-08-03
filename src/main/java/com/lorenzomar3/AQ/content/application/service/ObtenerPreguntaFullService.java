package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerPreguntaFullUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PreguntaSimpleFullDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerPreguntaFullService implements ObtenerPreguntaFullUseCase {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public ObtenerPreguntaFullService(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PreguntaSimpleFullDTO obtenerFull(Long id) {
        PreguntaSimple preguntaSimple = preguntaSimpleRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new PreguntaSimpleFullDTO(preguntaSimple.getId(), preguntaSimple.getTitulo(),
                preguntaSimple.getDescripcion(), preguntaSimple.getIdDuenio(), preguntaSimple.getFechaDeCreacion(),
                preguntaSimple.getUltimaActualizacion(), preguntaSimple.getTipo(),
                preguntaSimple.getIntentosParaQueDejeDeSerCriticoDisponible(), preguntaSimple.getImagenTitulo(),
                preguntaSimple.getRespuestaPrecisa(), preguntaSimple.getRespuestaEstablecida());
    }
}
