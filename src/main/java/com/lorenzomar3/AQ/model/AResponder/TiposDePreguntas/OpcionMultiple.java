package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Entity
@NoArgsConstructor
public class OpcionMultiple extends Pregunta<RespuestaDePreguntaDTO> {


    public OpcionMultiple(String titulo, List<Opcion> listaDeOpciones) {
        super(titulo);
        this.listaDeOpciones = listaDeOpciones;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpciones;

    @Transient
    HashMap<Long, Boolean> mapDeOpcionesConSuValidez = new HashMap<>();

    public OpcionMultiple(String titulo) {
        super(titulo);
    }

    @PostLoad
    public void init() {

        listaDeOpciones.forEach(op -> {
            mapDeOpcionesConSuValidez.put(op.getId(), op.getEsLaOpcionVerdadera());
        });

    }

    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuesta) {

        return respuesta.getListaDeOpciones()
                .stream().allMatch(opIngresado -> verificarCoincidencias(opIngresado));
    }

    public Boolean verificarCoincidencias(Opcion opcion) {
        return mapDeOpcionesConSuValidez.get(opcion.getId()).equals(opcion.getEsLaOpcionVerdadera());
    }


}
