package com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.tipoCompuesto;

import com.lorenzomar3.AQ.model.AResponder.Respuesta;
import com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.Pregunta;
import jakarta.persistence.Entity;

@Entity
public class PreguntaCompuesto extends Pregunta {



    @Override
    public Boolean laRespuestaEsCorrecta(Respuesta respuesta) {
        return null;
    }
}
