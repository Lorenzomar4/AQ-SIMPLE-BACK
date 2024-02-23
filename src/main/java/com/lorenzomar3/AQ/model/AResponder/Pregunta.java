package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.model.JerarquiaEnum;
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
    public String respuestaTexto;

    public Integer intentosParaQueDejeDeSerCriticoDisponible = 0;

    @Column(length = 1500)
    public String imagen;

    @Column(length = 1500)
    public String imagenRespuesta;


    public Pregunta(String titulo, String respuestaTexto) {
        super(titulo);
        this.tipo = JerarquiaEnum.PREGUNTA;
        this.respuestaTexto = respuestaTexto;
    }

    public Pregunta() {

    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean contieneCritico() {
        return intentosParaQueDejeDeSerCriticoDisponible > 0;
    }

    @Override
    public Integer numeroDePreguntas() {
        return 0;
    }
}
