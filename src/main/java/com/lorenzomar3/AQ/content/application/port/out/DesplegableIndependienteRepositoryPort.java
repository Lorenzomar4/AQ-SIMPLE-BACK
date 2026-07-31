package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;

import java.util.Optional;

public interface DesplegableIndependienteRepositoryPort {

    DesplegableIndependiente save(DesplegableIndependiente desplegableIndependiente);

    Optional<DesplegableIndependiente> findById(Long id);

    void deleteById(Long id);
}
