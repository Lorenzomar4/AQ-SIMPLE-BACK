package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Entity
@Getter
@Setter
public class DesplegableIndependiente extends Pregunta<List<RespuestaDeDesplegableIndependiente>> {


    @OneToMany
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    List<SeleccionUnica> listaDePreguntasConOpcionUnicas;


    @Transient
    HashMap<Long, Long> preguntaConSuIdDeLaRespuestaCorrespondiente = new HashMap<>();

    @PostConstruct
    public void init() {

        /*

        listaDePreguntasConOpcionUnicas.forEach(pregunta -> {
                    preguntaConSuIdDeLaRespuestaCorrespondiente.put(pregunta.getId(), pregunta.getIdDeLaRespuestaCorrecta());
                }
        );
        */

    }

    @Override
    public boolean laRespuestaEsCorrecta(List<RespuestaDeDesplegableIndependiente> listaRespuesta) {
        return listaRespuesta.stream().allMatch(resp ->
                preguntaConSuIdDeLaRespuestaCorrespondiente.get(resp.getIdDeLaPregunta())
                        .equals(resp.getIdDeLaRespuestaSeleccionada())
        );
    }


}
