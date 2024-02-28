package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.AsignadorDeTipoALasPreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.RespuestaDeDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
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
public class DesplegableIndependiente extends Pregunta {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    @JsonView(View.JustToAnswer.class)
    List<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas;


    @Transient
    HashMap<Long, Long> preguntaConSuIdDeLaRespuestaCorrespondiente = new HashMap<>();

    public DesplegableIndependiente(String titulo, List<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas) {
        super(titulo);
        this.listaDePreguntasConOpcionUnicas = listaDePreguntasConOpcionUnicas;

    }

    @PostLoad
    public void init() {
        /*
        listaDePreguntasConOpcionUnicas.forEach(pregunta -> {
                    preguntaConSuIdDeLaRespuestaCorrespondiente.put(pregunta.getId(), pregunta.filtrarLaOpcionCorrecta());
                }
        );
        *
         */
    }


    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

        List<SeleccionUnicaParaDesplegableIndependiente> lista = respuestaDePreguntaDTO
                .getListaDeSeleccionesUnicasParaDesplegableIndependiente();

        return lista.stream().allMatch(this::verificarCoincidecia);

    }

    public Boolean verificarCoincidecia(SeleccionUnicaParaDesplegableIndependiente seleccionUnica) {
        return true;//preguntaConSuIdDeLaRespuestaCorrespondiente.get(seleccionUnica.getId()).equals(seleccionUnica.filtrarLaOpcionCorrecta());
    }


}
