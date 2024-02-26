package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.List;

@Entity
public class OpcionMultiple extends Pregunta<List<Long>> {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    public List<Opcion> listaDeOpciones;

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
