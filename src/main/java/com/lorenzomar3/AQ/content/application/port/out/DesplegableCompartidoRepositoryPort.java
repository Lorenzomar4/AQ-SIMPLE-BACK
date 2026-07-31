package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;

import java.util.Optional;

public interface DesplegableCompartidoRepositoryPort {

    DesplegableCompartido save(DesplegableCompartido desplegableCompartido);

    Optional<DesplegableCompartido> findById(Long id);

    void deleteById(Long id);
}
