package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.SeleccionUnicaMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.SeleccionUnicaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SeleccionUnicaJpaAdapter implements SeleccionUnicaRepositoryPort {

    private final SeleccionUnicaJpaRepository seleccionUnicaJpaRepository;
    private final SeleccionUnicaMapper seleccionUnicaMapper;

    public SeleccionUnicaJpaAdapter(SeleccionUnicaJpaRepository seleccionUnicaJpaRepository, SeleccionUnicaMapper seleccionUnicaMapper) {
        this.seleccionUnicaJpaRepository = seleccionUnicaJpaRepository;
        this.seleccionUnicaMapper = seleccionUnicaMapper;
    }

    @Override
    public SeleccionUnica save(SeleccionUnica seleccionUnica) {
        return seleccionUnicaMapper.toDomain(
                seleccionUnicaJpaRepository.save(seleccionUnicaMapper.toEntity(seleccionUnica))
        );
    }

    @Override
    public Optional<SeleccionUnica> findById(Long id) {
        return seleccionUnicaJpaRepository.findById(id).map(seleccionUnicaMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        seleccionUnicaJpaRepository.deleteById(id);
    }
}
