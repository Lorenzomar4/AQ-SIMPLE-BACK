package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;

import java.util.Optional;

public interface VerdaderoOFalsoRepositoryPort {

    VerdaderoOFalso save(VerdaderoOFalso verdaderoOFalso);

    Optional<VerdaderoOFalso> findById(Long id);

    void deleteById(Long id);
}
