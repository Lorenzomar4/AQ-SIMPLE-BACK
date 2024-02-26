package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class VerdaderoOFalso extends Pregunta<Boolean> {

    public Boolean respuestaVerdadera;

    public VerdaderoOFalso(String titulo, Boolean respuestaVerdadera) {
        super(titulo);
        this.respuestaVerdadera = respuestaVerdadera;
    }

    @Override
    public boolean laRespuestaEsCorrecta(Boolean respuesta) {
        return respuestaVerdadera == respuesta;
    }
}
