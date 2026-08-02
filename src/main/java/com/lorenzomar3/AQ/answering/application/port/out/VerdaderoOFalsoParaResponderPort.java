package com.lorenzomar3.AQ.answering.application.port.out;

import com.lorenzomar3.AQ.answering.domain.VerdaderoOFalsoParaResponder;

import java.util.Optional;

public interface VerdaderoOFalsoParaResponderPort {

    Optional<VerdaderoOFalsoParaResponder> findById(Long id);

    VerdaderoOFalsoParaResponder save(VerdaderoOFalsoParaResponder pregunta);
}
