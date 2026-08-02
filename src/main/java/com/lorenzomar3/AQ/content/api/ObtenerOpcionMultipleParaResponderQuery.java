package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerOpcionMultipleParaResponderQuery {

    Optional<OpcionMultipleParaResponderView> obtenerPorId(Long id);
}
