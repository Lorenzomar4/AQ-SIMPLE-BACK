package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.Verificador;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Entity
@NoArgsConstructor
public class OpcionMultiple extends Pregunta {


    public OpcionMultiple(String titulo, List<Opcion> listaDeOpciones) {
        super(titulo);
        this.listaDeOpciones = listaDeOpciones;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpciones;


    public OpcionMultiple(String titulo) {
        super(titulo);
    }

    @PostLoad
    void init() {
        Collections.shuffle(listaDeOpciones);
    }

    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuesta) {
        Verificador<Boolean, Opcion> verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDeOpciones, respuesta.getListaDeOpciones());
    }


}
