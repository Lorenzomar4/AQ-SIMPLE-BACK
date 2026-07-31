package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.SeleccionUnica;

import java.util.Optional;

public interface SeleccionUnicaRepositoryPort {

    SeleccionUnica save(SeleccionUnica seleccionUnica);

    Optional<SeleccionUnica> findById(Long id);

    void deleteById(Long id);
}
