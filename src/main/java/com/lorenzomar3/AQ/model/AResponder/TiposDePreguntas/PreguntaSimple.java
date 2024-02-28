package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class PreguntaSimple extends Pregunta<RespuestaDePreguntaDTO> {


    public String respuestaEstablecida;

    public PreguntaSimple(String titulo, String respuestaEstablecida) {
        super(titulo);
        this.respuestaEstablecida = respuestaEstablecida;
    }


    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getRespuestaBooleana();
    }
}
