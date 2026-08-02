package com.lorenzomar3.AQ.answering.domain;

public record OpcionDeDesplegableCompartidoParaResponder(Long id, String respuesta) implements OpcionVerificable<String> {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getValorCorrecto() {
        return respuesta;
    }
}
