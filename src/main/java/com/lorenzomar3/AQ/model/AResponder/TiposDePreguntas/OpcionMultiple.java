package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.OpcionDeSeleccionUnica;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Entity
@Setter
@Getter
public class OpcionMultiple extends Pregunta<List<OpcionDeMultipleEleccion>> {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    public List<OpcionDeMultipleEleccion> listaDeOpciones;

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
    public boolean laRespuestaEsCorrecta(List<OpcionDeMultipleEleccion> listaDeOpcionesElegidas) {

        Boolean verificarQueLasRespuestasIngresadasCoincidanConAsignadasALaPregunta =

                listaDeOpcionesElegidas.stream()
                        .allMatch(opcionElegida ->
                                mapPreguntaRespuesta.get(opcionElegida.getPregunta())
                                        .equals(opcionElegida.getRespuesta()));


        return verificarQueLasRespuestasIngresadasCoincidanConAsignadasALaPregunta;
    }
}
