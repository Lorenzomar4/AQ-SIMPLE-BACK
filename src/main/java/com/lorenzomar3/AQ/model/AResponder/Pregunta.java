package com.lorenzomar3.AQ.model.AResponder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;


@Entity
@Getter
@Setter
public class Pregunta extends  AResponder{

    @Column(length = 10500)
    String respuestaTexto;

    Integer intentosParaQueDejeDeSerCriticoDisponible = 0;

    @Column(length = 1500)
    String imagen;

    @Column(length = 1500)
    String imagenRespuesta;


    public Pregunta(String titulo) {
        super(titulo);
    }

    public Pregunta() {

    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean contieneCritico() {
        return intentosParaQueDejeDeSerCriticoDisponible >= 0;
    }

    @Override
    public Integer numeroDePreguntas() {
        return 1;
    }
}
