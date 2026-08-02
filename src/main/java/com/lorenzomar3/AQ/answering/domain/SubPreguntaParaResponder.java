package com.lorenzomar3.AQ.answering.domain;

import java.util.List;

public record SubPreguntaParaResponder(Long id, List<OpcionParaResponder> opciones) implements OpcionVerificable<Long> {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getValorCorrecto() {
        return opciones.stream().filter(OpcionParaResponder::esCorrecta).toList().get(0).id();
    }
}
