package com.lorenzomar3.AQ.answering.application.port.out;

import com.lorenzomar3.AQ.answering.domain.PreguntaSimpleParaResponder;

import java.util.Optional;

public interface PreguntaSimpleParaResponderPort {

    Optional<PreguntaSimpleParaResponder> findById(Long id);

    PreguntaSimpleParaResponder save(PreguntaSimpleParaResponder pregunta);
}
