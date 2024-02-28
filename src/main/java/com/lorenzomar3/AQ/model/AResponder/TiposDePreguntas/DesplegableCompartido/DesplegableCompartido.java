package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
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
public class DesplegableCompartido extends Pregunta<List<OpcionDeDesplegableCompartido>> {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_pregunta")
    @JsonView(View.JustToAnswer.class)
    public List<OpcionDeDesplegableCompartido> listaDeOpciones;

    @Transient
    HashMap<String, String> mapPreguntaRespuesta = new HashMap<>();

    public DesplegableCompartido(String titulo, List<OpcionDeDesplegableCompartido> listaDeOpciones) {
        super(titulo);
        this.listaDeOpciones = listaDeOpciones;
    }

    @PostLoad
    public void init() {
        listaDeOpciones.forEach(opcion -> {
                    mapPreguntaRespuesta.put(opcion.getPregunta(), opcion.getRespuesta());
                }
        );

        Collections.shuffle(listaDeOpciones);


    }

    @Override
    public boolean laRespuestaEsCorrecta(List<OpcionDeDesplegableCompartido> listaDeOpcionesElegidas) {

        Boolean verificarQueLasRespuestasIngresadasCoincidanConAsignadasALaPregunta =

                listaDeOpcionesElegidas.stream()
                        .allMatch(opcionElegida ->
                                mapPreguntaRespuesta.get(opcionElegida.getPregunta())
                                        .equals(opcionElegida.getRespuesta()));


        return verificarQueLasRespuestasIngresadasCoincidanConAsignadasALaPregunta;
    }

    @JsonView(View.JustToAnswer.class)
    public List<String> posiblesRespuestasParaCadaOpcion() {
        List<String> posiblesRespuesta = new ArrayList<>(listaDeOpciones.stream().map(op -> op.getRespuesta()).toList());
        Collections.shuffle(posiblesRespuesta);
        return posiblesRespuesta;

    }
}