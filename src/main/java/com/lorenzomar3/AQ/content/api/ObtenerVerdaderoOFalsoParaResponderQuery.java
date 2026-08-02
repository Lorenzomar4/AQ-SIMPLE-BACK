package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerVerdaderoOFalsoParaResponderQuery {

    Optional<VerdaderoOFalsoParaResponderView> obtenerPorId(Long id);
}
