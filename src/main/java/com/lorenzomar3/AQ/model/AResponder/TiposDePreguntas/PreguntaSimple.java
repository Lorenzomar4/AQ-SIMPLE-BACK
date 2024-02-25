package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;

public class PreguntaSimple extends Pregunta<Boolean> {

    public String respuestaEstablecida;

    @Override
    public boolean laRespuestaEsCorrecta(Boolean respuesta) {
        return respuesta;
    }
}
