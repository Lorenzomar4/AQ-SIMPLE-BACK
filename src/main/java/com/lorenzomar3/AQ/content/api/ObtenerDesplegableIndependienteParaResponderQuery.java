package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerDesplegableIndependienteParaResponderQuery {

    Optional<DesplegableIndependienteParaResponderView> obtenerPorId(Long id);
}
