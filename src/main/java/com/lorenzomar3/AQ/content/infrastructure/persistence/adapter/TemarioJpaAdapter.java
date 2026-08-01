package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.AResponderChildRef;
import com.lorenzomar3.AQ.content.application.port.out.AResponderItemDetail;
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
    private final com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo;

    public TemarioJpaAdapter(TemarioJpaRepository temarioJpaRepository,
                              TemarioMapper temarioMapper,
                              com.lorenzomar3.AQ.Repository.TemarioRepository temarioRepositoryViejo,
                              com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo) {
        this.temarioJpaRepository = temarioJpaRepository;
        this.temarioMapper = temarioMapper;
        this.temarioRepositoryViejo = temarioRepositoryViejo;
        this.aResponderRepositoryViejo = aResponderRepositoryViejo;
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

    @Override
    public List<AResponderChildRef> findDirectChildren(Long id) {
        return aResponderRepositoryViejo.getIssueItems(id).stream()
                .filter(item -> !item.getId().equals(id))
                .map(item -> new AResponderChildRef(item.getId(), item.getType()))
                .toList();
    }

    @Override
    public List<AResponderItemDetail> findIssueItems(Long id) {
        return aResponderRepositoryViejo.getIssueItems(id).stream()
                .map(item -> new AResponderItemDetail(item.getId(), item.getType(), item.getName(),
                        item.getCreationDate(), item.getIsCritic(), item.getNumberOfQuestions()))
                .toList();
    }
}
