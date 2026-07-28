package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.TemarioMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.TemarioJpaRepository;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemarioJpaAdapter implements TemarioRepositoryPort {

    private final TemarioJpaRepository temarioJpaRepository;
    private final TemarioMapper temarioMapper;

    public TemarioJpaAdapter(TemarioJpaRepository temarioJpaRepository, TemarioMapper temarioMapper) {
        this.temarioJpaRepository = temarioJpaRepository;
        this.temarioMapper = temarioMapper;
    }

    @Override
    public List<Temario> findAllCuestionarios() {
        return temarioJpaRepository.findByTipo(TipoAResponder.CUESTIONARIO)
                .stream()
                .map(temarioMapper::toDomain)
                .toList();
    }
}
