package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;

public class VerdaderoOFalso extends Pregunta<Boolean> {

    public Boolean respuestaVerdadera;

    @Override
    public boolean laRespuestaEsCorrecta(Boolean respuesta) {
        return respuestaVerdadera == respuesta;
    }
}
