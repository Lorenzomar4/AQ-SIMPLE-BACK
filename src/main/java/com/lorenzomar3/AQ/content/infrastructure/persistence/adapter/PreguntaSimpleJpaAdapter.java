package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.PreguntaSimpleMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.PreguntaSimpleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PreguntaSimpleJpaAdapter implements PreguntaSimpleRepositoryPort {

    private final PreguntaSimpleJpaRepository preguntaSimpleJpaRepository;
    private final PreguntaSimpleMapper preguntaSimpleMapper;

    public PreguntaSimpleJpaAdapter(PreguntaSimpleJpaRepository preguntaSimpleJpaRepository, PreguntaSimpleMapper preguntaSimpleMapper) {
        this.preguntaSimpleJpaRepository = preguntaSimpleJpaRepository;
        this.preguntaSimpleMapper = preguntaSimpleMapper;
    }

    @Override
    public PreguntaSimple save(PreguntaSimple preguntaSimple) {
        return preguntaSimpleMapper.toDomain(
                preguntaSimpleJpaRepository.save(preguntaSimpleMapper.toEntity(preguntaSimple))
        );
    }

    @Override
    public Optional<PreguntaSimple> findById(Long id) {
        return preguntaSimpleJpaRepository.findById(id).map(preguntaSimpleMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        preguntaSimpleJpaRepository.deleteById(id);
    }
}
