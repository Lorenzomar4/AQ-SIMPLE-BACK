package com.lorenzomar3.AQ.model;

import com.lorenzomar3.AQ.model.AResponder.AResponder;

import java.util.List;

public class Temario extends AResponder {
    @Override
    public List<AResponder> contenidoAResponder() {
        return null;
    }

    @Override
    public boolean contieneCritico() {
        return false;
    }

    @Override
    public Integer numeroDePreguntas() {
        return null;
    }

    @Override
    public void asignacionDeTipo() {

    }
}
