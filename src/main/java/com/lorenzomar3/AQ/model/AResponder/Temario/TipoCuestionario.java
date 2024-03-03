package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.TipoAResponder;

public class TipoCuestionario extends TipoDeTemario {

    private static TipoCuestionario instace;

    private TipoCuestionario (){

    }

    public static TipoCuestionario getInstance(){
        if(instace==null){
            instace = new TipoCuestionario();
        }
        return instace;
    }

    @Override
    public TipoAResponder retornarElTipoDeTemario() {
        return TipoAResponder.TEMA;
    }
}
