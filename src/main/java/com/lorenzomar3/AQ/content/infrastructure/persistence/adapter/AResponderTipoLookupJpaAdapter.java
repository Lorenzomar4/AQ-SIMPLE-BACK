package com.lorenzomar3.AQ.content.infrastructure.persistence.adapter;

import com.lorenzomar3.AQ.content.application.port.out.AResponderTipoLookupPort;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AResponderTipoLookupJpaAdapter implements AResponderTipoLookupPort {

    private final com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo;

    public AResponderTipoLookupJpaAdapter(com.lorenzomar3.AQ.Repository.AResponderRepository aResponderRepositoryViejo) {
        this.aResponderRepositoryViejo = aResponderRepositoryViejo;
    }

    @Override
    public Optional<TipoAResponder> findTipoById(Long id) {
        return aResponderRepositoryViejo.findById(id).map(AResponder::getTipo);
    }
}
