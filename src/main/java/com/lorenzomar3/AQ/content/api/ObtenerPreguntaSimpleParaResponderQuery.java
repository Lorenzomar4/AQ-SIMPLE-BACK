package com.lorenzomar3.AQ.content.api;

import java.util.Optional;

public interface ObtenerPreguntaSimpleParaResponderQuery {

    Optional<PreguntaSimpleParaResponderView> obtenerPorId(Long id);
}
