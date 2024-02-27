package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Entity
@Setter
@Getter
public class DesplegableCompartido extends Pregunta<List<OpcionDeDesplegableCompartido>> {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    @JsonView(View.JustToAnswer.class)
    public List<OpcionDeDesplegableCompartido> listaDeOpciones;

    @Transient
    HashMap<String, String> mapPreguntaRespuesta = new HashMap<>();

    @PostConstruct
    public void init() {
        listaDeOpciones.forEach(opcion -> {
                    mapPreguntaRespuesta.put(opcion.getPregunta(), opcion.getRespuesta());
                }
        );
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
}
