package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.Verificador;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class DesplegableCompartido extends Pregunta {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pregunta")
    @JsonView(View.JustToAnswer.class)
    public List<OpcionDeDesplegableCompartido> listaDeOpciones;



    public DesplegableCompartido(String titulo, List<OpcionDeDesplegableCompartido> listaDeOpciones) {
        super(titulo);
        this.listaDeOpciones = listaDeOpciones;
    }


    @PostLoad
    public void init() {
        Collections.shuffle(listaDeOpciones);
    }

    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        Verificador<String ,OpcionDeDesplegableCompartido > verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDeOpciones,respuestaDePreguntaDTO.getListaDeOpcionesParaDesplegableCompartidos() );
    }


    @JsonView(View.JustToAnswer.class)
    public List<String> posiblesRespuestasParaCadaOpcion() {
        List<String> posiblesRespuesta = new ArrayList<>(listaDeOpciones.stream().map(op -> op.getRespuestaCorrecta()).toList());
        Collections.shuffle(posiblesRespuesta);
        return posiblesRespuesta;

    }
}
