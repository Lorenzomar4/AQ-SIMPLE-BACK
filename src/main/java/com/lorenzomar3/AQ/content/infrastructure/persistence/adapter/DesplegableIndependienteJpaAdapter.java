package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.DesplegableIndependienteMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.DesplegableIndependienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DesplegableIndependienteJpaAdapter implements DesplegableIndependienteRepositoryPort {

    private final DesplegableIndependienteJpaRepository desplegableIndependienteJpaRepository;
    private final DesplegableIndependienteMapper desplegableIndependienteMapper;

    public DesplegableIndependienteJpaAdapter(DesplegableIndependienteJpaRepository desplegableIndependienteJpaRepository, DesplegableIndependienteMapper desplegableIndependienteMapper) {
        this.desplegableIndependienteJpaRepository = desplegableIndependienteJpaRepository;
        this.desplegableIndependienteMapper = desplegableIndependienteMapper;
    }

    @Override
    public DesplegableIndependiente save(DesplegableIndependiente desplegableIndependiente) {
        return desplegableIndependienteMapper.toDomain(
                desplegableIndependienteJpaRepository.save(desplegableIndependienteMapper.toEntity(desplegableIndependiente))
        );
    }

    @Override
    public Optional<DesplegableIndependiente> findById(Long id) {
        return desplegableIndependienteJpaRepository.findById(id).map(desplegableIndependienteMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        desplegableIndependienteJpaRepository.deleteById(id);
    }
}
