package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.util.Optional;

public interface AResponderTipoLookupPort {

    Optional<TipoAResponder> findTipoById(Long id);
}
