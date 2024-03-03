package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.IPreguntaVariasOpciones;
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
public class DesplegableCompartido extends Pregunta implements IPreguntaVariasOpciones<String, OpcionDeDesplegableCompartido> {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pregunta")

    public List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido;


    public DesplegableCompartido(String titulo, List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido) {
        super(titulo);
        this.listaDeOpcionDesplegableCompartido = listaDeOpcionDesplegableCompartido;
    }


    @PostLoad
    public void init() {
        Collections.shuffle(listaDeOpcionDesplegableCompartido);
    }


    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        Verificador<String, OpcionDeDesplegableCompartido> verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDeOpcionDesplegableCompartido, respuestaDePreguntaDTO.getListaDeOpcionesParaDesplegableCompartidos());
    }

    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

    }

    @Override
    public List<OpcionDeDesplegableCompartido> listaDeOpciones() {
        return listaDeOpcionDesplegableCompartido;
    }

    @Override
    public List<OpcionDeDesplegableCompartido> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeOpcionesParaDesplegableCompartidos();
    }


    @JsonView(View.JustToAnswer.class)
    public List<String> posiblesRespuestasParaCadaOpcion() {
        List<String> posiblesRespuesta = new ArrayList<>(listaDeOpcionDesplegableCompartido.stream().map(op -> op.getRespuestaCorrecta()).toList());
        Collections.shuffle(posiblesRespuesta);
        return posiblesRespuesta;

    }
}
