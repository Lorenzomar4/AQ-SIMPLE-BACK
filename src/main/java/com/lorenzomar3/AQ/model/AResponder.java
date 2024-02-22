package com.lorenzomar3.AQ.model;

import java.time.LocalDate;
import java.util.List;

public abstract class AResponder {

    public Long id ;

    public String titulo;

    public LocalDate fechaDeCreacion;

    public LocalDate ultimaVezAbierto;

    public LocalDate ultimaActualizacion;

    public JerarquiaEnum tipo;

    public abstract List<AResponder> contenidoAResponder();

    public abstract boolean contieneCritico();

    public AResponder(String titulo) {
        this.titulo = titulo;
    }
}
