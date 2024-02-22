package com.lorenzomar3.AQ.model;

import java.util.List;

public class APregunta extends  AResponder{

    public APregunta(String titulo) {
        super(titulo);
    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return null;
    }

    @Override
    public boolean contieneCritico() {
        return false;
    }
}
