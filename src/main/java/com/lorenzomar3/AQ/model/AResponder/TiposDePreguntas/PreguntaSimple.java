package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class PreguntaSimple extends Pregunta {


    public String respuestaEstablecida;

    public PreguntaSimple(String titulo, String respuestaEstablecida) {
        super(titulo);
        this.respuestaEstablecida = respuestaEstablecida;
    }

    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getRespuestaBooleana();
    }
}
