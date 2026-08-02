package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerDesplegableCompartidoParaResponderQuery {

    Optional<DesplegableCompartidoParaResponderView> obtenerPorId(Long id);
}
