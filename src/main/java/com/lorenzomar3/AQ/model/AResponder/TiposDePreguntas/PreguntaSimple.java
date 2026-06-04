package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;

@Entity
@NoArgsConstructor
@Setter
@Getter
public class PreguntaSimple extends Pregunta {

    public Boolean respuestaPrecisa=false;
    @JsonView(View.Full.class)


    @Column(length = 50000)
    public String respuestaEstablecida;

    public PreguntaSimple(String titulo, String respuestaEstablecida) {
        super(titulo);
        this.respuestaEstablecida = respuestaEstablecida;
    }

    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuesta) {



        return respuesta.respuestaBooleana();
    }
    //Usar en un futuro
    /*
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        String respuestaDelUsuario = respuestaDePreguntaDTO.getRespuestaInput();

        return  respuestaPrecisa ?  respuestaDelUsuario.equals(respuestaEstablecida) : respuestaDePreguntaDTO.getRespuestaBooleana();
    }
    */

    public PreguntaSimple inversar() {

        String respuesta = Jsoup.parse(respuestaEstablecida).text();
        PreguntaSimple preguntaSimple = new PreguntaSimple(respuesta, titulo);
        preguntaSimple.setTipo(tipo);

        return preguntaSimple;


    }

}
