package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.OpcionMultipleMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.OpcionMultipleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OpcionMultipleJpaAdapter implements OpcionMultipleRepositoryPort {

    private final OpcionMultipleJpaRepository opcionMultipleJpaRepository;
    private final OpcionMultipleMapper opcionMultipleMapper;

    public OpcionMultipleJpaAdapter(OpcionMultipleJpaRepository opcionMultipleJpaRepository, OpcionMultipleMapper opcionMultipleMapper) {
        this.opcionMultipleJpaRepository = opcionMultipleJpaRepository;
        this.opcionMultipleMapper = opcionMultipleMapper;
    }

    @Override
    public OpcionMultiple save(OpcionMultiple opcionMultiple) {
        return opcionMultipleMapper.toDomain(
                opcionMultipleJpaRepository.save(opcionMultipleMapper.toEntity(opcionMultiple))
        );
    }

    @Override
    public Optional<OpcionMultiple> findById(Long id) {
        return opcionMultipleJpaRepository.findById(id).map(opcionMultipleMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        opcionMultipleJpaRepository.deleteById(id);
    }
}
