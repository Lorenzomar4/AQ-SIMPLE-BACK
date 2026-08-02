package com.lorenzomar3.AQ.answering.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.answering.application.port.out.PreguntaSimpleParaResponderPort;
import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.PreguntaSimpleParaResponder;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.PreguntaSimpleEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PreguntaSimpleParaResponderAdapter implements PreguntaSimpleParaResponderPort {

    private final PreguntaSimpleJpaRepository preguntaSimpleJpaRepository;

    public PreguntaSimpleParaResponderAdapter(PreguntaSimpleJpaRepository preguntaSimpleJpaRepository) {
        this.preguntaSimpleJpaRepository = preguntaSimpleJpaRepository;
    }

    @Override
    public Optional<PreguntaSimpleParaResponder> findById(Long id) {
        return preguntaSimpleJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public PreguntaSimpleParaResponder save(PreguntaSimpleParaResponder pregunta) {
        PreguntaSimpleEntity entity = preguntaSimpleJpaRepository.findById(pregunta.getId()).orElseThrow();
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(
                pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible()
        );
        return toDomain(preguntaSimpleJpaRepository.save(entity));
    }

    private PreguntaSimpleParaResponder toDomain(PreguntaSimpleEntity entity) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());

        PreguntaSimpleParaResponder pregunta = new PreguntaSimpleParaResponder();
        pregunta.setId(entity.getId());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
