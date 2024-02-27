package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;

@Entity
@NoArgsConstructor
public class OpcionMultiple extends Pregunta<List<Long>> {



    public OpcionMultiple(String titulo, List<Opcion> listaDeOpciones) {
        super(titulo);
        this.listaDeOpciones = listaDeOpciones;
    }

    @OneToMany(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpciones;

    public OpcionMultiple(String titulo) {
        super(titulo);
    }

    @Override
    public boolean laRespuestaEsCorrecta(List<Long> respuesta) {
        Boolean condicion1 = todasLasOpcionesValidasEstanEnLaListaIngresadaPorElUsuario(respuesta);
        Boolean condicion2 = todasLasOpcionesInvalidasNoDebenEstarEnLaListaIngresadaPorElUsuario(respuesta);

        return condicion1 && condicion2;
    }



    private List<Long> listaDeOpcionesValidas() {
        return listaDeOpciones.stream().filter(op -> op.getEsLaOpcionVerdadera()).map(op -> op.getId()).toList();
    }

    private List<Long> listaDeOpcionesInvalidas() {
        return listaDeOpciones.stream().filter(op -> !op.getEsLaOpcionVerdadera()).map(op -> op.getId()).toList();
    }


    private Boolean todasLasOpcionesValidasEstanEnLaListaIngresadaPorElUsuario(List<Long> respuesta) {
        return new HashSet<>(respuesta).containsAll(listaDeOpcionesValidas());
    }

    private Boolean todasLasOpcionesInvalidasNoDebenEstarEnLaListaIngresadaPorElUsuario(List<Long> larespuesta) {
        return listaDeOpcionesInvalidas().stream().noneMatch(opDeRespuesta -> larespuesta.contains(opDeRespuesta));
    }

}
