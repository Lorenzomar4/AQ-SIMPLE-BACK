package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.VerdaderoOFalsoMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.VerdaderoOFalsoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VerdaderoOFalsoJpaAdapter implements VerdaderoOFalsoRepositoryPort {

    private final VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository;
    private final VerdaderoOFalsoMapper verdaderoOFalsoMapper;

    public VerdaderoOFalsoJpaAdapter(VerdaderoOFalsoJpaRepository verdaderoOFalsoJpaRepository, VerdaderoOFalsoMapper verdaderoOFalsoMapper) {
        this.verdaderoOFalsoJpaRepository = verdaderoOFalsoJpaRepository;
        this.verdaderoOFalsoMapper = verdaderoOFalsoMapper;
    }

    @Override
    public VerdaderoOFalso save(VerdaderoOFalso verdaderoOFalso) {
        return verdaderoOFalsoMapper.toDomain(
                verdaderoOFalsoJpaRepository.save(verdaderoOFalsoMapper.toEntity(verdaderoOFalso))
        );
    }

    @Override
    public Optional<VerdaderoOFalso> findById(Long id) {
        return verdaderoOFalsoJpaRepository.findById(id).map(verdaderoOFalsoMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        verdaderoOFalsoJpaRepository.deleteById(id);
    }
}
