package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.DesplegableCompartidoMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.DesplegableCompartidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DesplegableCompartidoJpaAdapter implements DesplegableCompartidoRepositoryPort {

    private final DesplegableCompartidoJpaRepository desplegableCompartidoJpaRepository;
    private final DesplegableCompartidoMapper desplegableCompartidoMapper;

    public DesplegableCompartidoJpaAdapter(DesplegableCompartidoJpaRepository desplegableCompartidoJpaRepository, DesplegableCompartidoMapper desplegableCompartidoMapper) {
        this.desplegableCompartidoJpaRepository = desplegableCompartidoJpaRepository;
        this.desplegableCompartidoMapper = desplegableCompartidoMapper;
    }

    @Override
    public DesplegableCompartido save(DesplegableCompartido desplegableCompartido) {
        return desplegableCompartidoMapper.toDomain(
                desplegableCompartidoJpaRepository.save(desplegableCompartidoMapper.toEntity(desplegableCompartido))
        );
    }

    @Override
    public Optional<DesplegableCompartido> findById(Long id) {
        return desplegableCompartidoJpaRepository.findById(id).map(desplegableCompartidoMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        desplegableCompartidoJpaRepository.deleteById(id);
    }
}
