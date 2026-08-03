package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearPreguntaSimpleService implements CrearPreguntaUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public CrearPreguntaSimpleService(TemarioRepositoryPort temarioRepositoryPort, PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        PreguntaSimple preguntaSimple = new PreguntaSimple();
        preguntaSimple.setTitulo(postPreguntaDTO.titulo());
        preguntaSimple.setDescripcion(postPreguntaDTO.descripcion());
        preguntaSimple.setRespuestaEstablecida(postPreguntaDTO.respuestaEstablecida());
        preguntaSimple.setTipo(TipoAResponder.PREGUNTA_SIMPLE);
        preguntaSimple.setIdDuenio(temario.getId());
        preguntaSimple.setFechaDeCreacion(LocalDateTime.now());
        preguntaSimple.setIntentosParaQueDejeDeSerCriticoDisponible(0);
        preguntaSimple.setRespuestaPrecisa(false);

        preguntaSimpleRepositoryPort.save(preguntaSimple);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }
}
