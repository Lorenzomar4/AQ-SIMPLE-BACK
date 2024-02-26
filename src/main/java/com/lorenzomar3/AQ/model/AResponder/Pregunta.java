package com.lorenzomar3.AQ.model.AResponder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;


@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public abstract class Pregunta<T> extends AResponder {

    public Integer intentosParaQueDejeDeSerCriticoDisponible = 0;

    @Column(length = 1500)
    public String imagenTitulo;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "id_pregunta")
    public List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta;


    public Pregunta(String titulo, String imagenTitulo) {
        super(titulo);
        this.imagenTitulo = imagenTitulo;
    }

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
        return intentosParaQueDejeDeSerCriticoDisponible > 0;
    }

    @Override
    public Integer numeroDePreguntas() {
        return 0;
    }

    @Override
    public void asignarTipoSiSeAgregaDesdeCuestionario() {

        tipo = AsignadorDeTipoALasPreguntas.getInstance().asignarTipo(this);
    }

    public abstract boolean laRespuestaEsCorrecta(T respuesta);

}


