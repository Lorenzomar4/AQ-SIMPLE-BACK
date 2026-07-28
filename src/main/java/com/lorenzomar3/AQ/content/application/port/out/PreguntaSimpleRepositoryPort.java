package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.PreguntaSimple;

import java.util.Optional;

public interface PreguntaSimpleRepositoryPort {

    PreguntaSimple save(PreguntaSimple preguntaSimple);

    Optional<PreguntaSimple> findById(Long id);

    void deleteById(Long id);
}
