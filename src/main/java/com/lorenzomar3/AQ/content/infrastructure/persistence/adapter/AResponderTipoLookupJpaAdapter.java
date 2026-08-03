package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.AResponderTipoLookupPort;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.AResponderEntity;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.AResponderJpaRepository;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AResponderTipoLookupJpaAdapter implements AResponderTipoLookupPort {

    private final AResponderJpaRepository aResponderJpaRepository;

    public AResponderTipoLookupJpaAdapter(AResponderJpaRepository aResponderJpaRepository) {
        this.aResponderJpaRepository = aResponderJpaRepository;
    }

    @Override
    public Optional<TipoAResponder> findTipoById(Long id) {
        return aResponderJpaRepository.findById(id).map(AResponderEntity::getTipo);
    }
}
