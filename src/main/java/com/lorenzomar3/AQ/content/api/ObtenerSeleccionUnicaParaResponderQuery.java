package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerSeleccionUnicaParaResponderQuery {

    Optional<SeleccionUnicaParaResponderView> obtenerPorId(Long id);
}
