package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.OpcionMultiple;

import java.util.Optional;

public interface OpcionMultipleRepositoryPort {

    OpcionMultiple save(OpcionMultiple opcionMultiple);

    Optional<OpcionMultiple> findById(Long id);

    void deleteById(Long id);
}
