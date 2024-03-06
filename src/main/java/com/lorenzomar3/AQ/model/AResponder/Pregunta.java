package com.lorenzomar3.AQ.model.AResponder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.ITemaPregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public abstract class Pregunta extends AResponder implements IPregunta, ITemaPregunta {


    @JsonView(View.JustToAnswer.class)
    public Integer intentosParaQueDejeDeSerCriticoDisponible = 0;

    @Column(length = 1500)
    @JsonView(View.JustToAnswer.class)
    public String imagenTitulo;


    @JsonView(View.Full.class)
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pregunaDuenia", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta = new HashSet<>();


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
    public void asignacionDeTipo() {

        tipo = AsignadorDeTipoALasPreguntas.getInstance().asignarTipo(this);
    }

    public void agregarTeoria(TeoriaDeLaPregunta teoriaDeLaPregunta) {
        listaDeTeoriaDeLaPregunta.add(teoriaDeLaPregunta);
        teoriaDeLaPregunta.setPregunaDuenia(this);
    }

    @Override
    public List<Long> obtenerListaDeIdentificadoresDePreguntas() {
        return Collections.singletonList(this.id);
    }


}


