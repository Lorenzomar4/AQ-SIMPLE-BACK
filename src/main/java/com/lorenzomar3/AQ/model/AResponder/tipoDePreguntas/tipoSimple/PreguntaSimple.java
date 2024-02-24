package com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.tipoSimple;


import com.lorenzomar3.AQ.model.AResponder.Respuesta;
import com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.tipoSimple.PreguntaSimpleTipo;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class PreguntaSimple extends Pregunta {

    @Enumerated(EnumType.STRING)
    PreguntaSimpleTipo tipo;


    public PreguntaSimple(String titulo, String imagenTitulo) {
        super(titulo, imagenTitulo);
    }

    @Override
    public Boolean laRespuestaEsCorrecta(Respuesta respuesta) {
        return null;
    }
}
