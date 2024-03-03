package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.AsignadorDeTipoALasPreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.TipoAResponder;

public abstract class TipoDeTemario {


    public abstract TipoAResponder retornarElTipoDeTemario();

    public TipoAResponder retornarElTipoParaLaPreguntaOTemario(AResponder elemento) {
        return elemento instanceof Temario ? retornarElTipoDeTemario() : tipoParaPreguntas(elemento);
    }


    public TipoAResponder tipoParaPreguntas(AResponder pregunta) {


        AsignadorDeTipoALasPreguntas asignador = AsignadorDeTipoALasPreguntas.getInstance();
        return asignador.asignarTipo((Pregunta) pregunta);
    }


}
