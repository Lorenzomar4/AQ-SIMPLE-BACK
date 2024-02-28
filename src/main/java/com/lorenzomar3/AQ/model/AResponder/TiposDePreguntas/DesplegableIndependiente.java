package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.AsignadorDeTipoALasPreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependiente extends Pregunta<List<RespuestaDeDesplegableIndependiente>> {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    @JsonView(View.JustToAnswer.class)
    List<SeleccionUnica> listaDePreguntasConOpcionUnicas;


    @Transient
    HashMap<Long, Long> preguntaConSuIdDeLaRespuestaCorrespondiente = new HashMap<>();

    public DesplegableIndependiente(String titulo, List<SeleccionUnica> listaDePreguntasConOpcionUnicas) {
        super(titulo);
        this.listaDePreguntasConOpcionUnicas = listaDePreguntasConOpcionUnicas;

        this.listaDePreguntasConOpcionUnicas.forEach(su -> {
            AsignadorDeTipoALasPreguntas.getInstance().asignarTipo(su);
        });

    }

    @PostLoad
    public void init() {
        listaDePreguntasConOpcionUnicas.forEach(pregunta -> {
                    preguntaConSuIdDeLaRespuestaCorrespondiente.put(pregunta.getId(), pregunta.filtrarLaOpcionCorrecta());
                }
        );
    }

    @PrePersist
    public void preSave() {
        listaDePreguntasConOpcionUnicas.forEach(su -> {
            su.setTipo(AsignadorDeTipoALasPreguntas.getInstance().asignarTipo(su));
        });
    }

    @Override
    public boolean laRespuestaEsCorrecta(List<RespuestaDeDesplegableIndependiente> listaRespuesta) {
        return listaRespuesta.stream().allMatch(resp ->
                preguntaConSuIdDeLaRespuestaCorrespondiente.get(resp.getIdDeLaPregunta())
                        .equals(resp.getIdDeLaRespuestaSeleccionada())
        );
    }


}
