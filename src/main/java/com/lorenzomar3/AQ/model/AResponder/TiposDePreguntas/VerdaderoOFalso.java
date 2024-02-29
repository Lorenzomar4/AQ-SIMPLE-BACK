package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class VerdaderoOFalso extends Pregunta {

    public Boolean respuestaVerdadera;

    public VerdaderoOFalso(String titulo, Boolean respuestaVerdadera) {
        super(titulo);
        this.respuestaVerdadera = respuestaVerdadera;
    }


    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaVerdadera == respuestaDePreguntaDTO.getRespuestaBooleana();
    }
}
