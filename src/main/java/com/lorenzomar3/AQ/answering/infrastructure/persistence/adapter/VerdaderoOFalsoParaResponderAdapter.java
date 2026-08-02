package com.lorenzomar3.AQ.answering.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.answering.application.port.out.VerdaderoOFalsoParaResponderPort;
import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.VerdaderoOFalsoParaResponder;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.VerdaderoOFalsoEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.VerdaderoOFalsoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerdaderoOFalsoParaResponderAdapter implements VerdaderoOFalsoParaResponderPort {

    private final VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository;

    public VerdaderoOFalsoParaResponderAdapter(VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository) {
        this.verdaderoOFalsoJpaRepository = verdaderoOFalsoJpaRepository;
    }

    @Override
    public Optional<VerdaderoOFalsoParaResponder> findById(Long id) {
        return verdaderoOFalsoJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public VerdaderoOFalsoParaResponder save(VerdaderoOFalsoParaResponder pregunta) {
        VerdaderoOFalsoEntity entity = verdaderoOFalsoJpaRepository.findById(pregunta.getId()).orElseThrow();
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(
                pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible()
        );
        return toDomain(verdaderoOFalsoJpaRepository.save(entity));
    }

    private VerdaderoOFalsoParaResponder toDomain(VerdaderoOFalsoEntity entity) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());

        VerdaderoOFalsoParaResponder pregunta = new VerdaderoOFalsoParaResponder();
        pregunta.setId(entity.getId());
        pregunta.setRespuestaVerdadera(entity.getRespuestaVerdadera());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
