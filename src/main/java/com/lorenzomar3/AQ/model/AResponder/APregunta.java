package com.lorenzomar3.AQ.model.AResponder;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class APregunta extends  AResponder{

    @Column(length = 10500)
    String respuestaTexto;

    Integer descuentoDeRepregunta = 0;

    @Column(length = 1500)
    String imagen;

    @Column(length = 1500)
    String imagenRespuesta;


    public APregunta(String titulo) {
        super(titulo);
    }

    public APregunta() {

    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return null;
    }

    @Override
    public boolean contieneCritico() {
        return false;
    }
}
