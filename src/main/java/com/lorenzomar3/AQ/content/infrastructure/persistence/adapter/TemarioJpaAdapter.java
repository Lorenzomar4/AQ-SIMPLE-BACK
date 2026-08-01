package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.persistence.mapper.TemarioMapper;
import com.lorenzomar3.AQ.content.infrastructure.persistence.repository.TemarioJpaRepository;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TemarioJpaAdapter implements TemarioRepositoryPort {

    private final TemarioJpaRepository temarioJpaRepository;
    private final TemarioMapper temarioMapper;
    private final com.lorenzomar3.AQ.Repository.TemarioRepository temarioRepositoryViejo;

    public TemarioJpaAdapter(TemarioJpaRepository temarioJpaRepository,
                              TemarioMapper temarioMapper,
                              com.lorenzomar3.AQ.Repository.TemarioRepository temarioRepositoryViejo) {
        this.temarioJpaRepository = temarioJpaRepository;
        this.temarioMapper = temarioMapper;
        this.temarioRepositoryViejo = temarioRepositoryViejo;
    }

    @Override
    public List<Temario> findAllCuestionarios() {
        return temarioJpaRepository.findByTipo(TipoAResponder.CUESTIONARIO)
                .stream()
                .map(temarioMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Temario> findById(Long id) {
        return temarioJpaRepository.findById(id).map(temarioMapper::toDomain);
    }

    @Override
    public Temario save(Temario temario) {
        return temarioMapper.toDomain(temarioJpaRepository.save(temarioMapper.toEntity(temario)));
    }

    @Override
    public void deleteById(Long id) {
        temarioRepositoryViejo.deleteById(id);
    }
}
