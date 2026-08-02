package com.lorenzomar3.AQ.answering.domain;

public record OpcionParaResponder(Long id, Boolean esCorrecta) implements OpcionVerificable<Boolean> {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Boolean getValorCorrecto() {
        return esCorrecta;
    }
}
