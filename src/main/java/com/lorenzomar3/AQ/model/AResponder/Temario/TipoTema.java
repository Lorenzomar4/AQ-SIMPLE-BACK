package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.model.TipoAResponder;

public class TipoTema extends TipoDeTemario {

    private static TipoTema instace;

    private TipoTema() {

    }

    public static TipoTema getInstance() {
        if (instace == null) {
            instace = new TipoTema();
        }
        return instace;
    }

    @Override
    public TipoAResponder retornarElTipoDeTemario() {
        return TipoAResponder.SUBTEMA;
    }
}
