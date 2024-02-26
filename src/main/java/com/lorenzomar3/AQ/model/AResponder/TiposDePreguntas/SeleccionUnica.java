package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SeleccionUnica extends Pregunta<Long> {


    Long idDeLaRespuestaCorrecta;

    @OneToMany(fetch = FetchType.LAZY ,  cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta")
    public List<Opcion> listaDeOpcionesDisponible = new ArrayList<>();

    public SeleccionUnica(String titulo, List<Opcion> listaDeOpcionesDisponible) {
        super(titulo);
        setListaDeOpcionesDisponible(listaDeOpcionesDisponible);
        this.listaDeOpcionesDisponible = listaDeOpcionesDisponible;
    }

    @Override
    public boolean laRespuestaEsCorrecta(Long idRespuestaCorrecta) {
        return idDeLaRespuestaCorrecta.equals(idRespuestaCorrecta);
    }


    public void setListaDeOpcionesDisponible(List<Opcion> lista) {
        if (!existeUnaOpcionVerdaderaUnicamente(lista)) {
            throw new ErrorDeNegocio("¡Asegurese de que haya solamente una opcion valida!");
        }


        lista.forEach(op -> agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(op));

    }

    private Boolean existeUnaOpcionVerdaderaUnicamente(List<Opcion> lista) {
        int numeroDeOpcionesVerdaderas = lista.stream().filter(op -> op.getEsLaOpcionVerdadera()).toList().size();

        return numeroDeOpcionesVerdaderas == 1;

    }

    public void agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(Opcion opcion) {

        if (opcion.getEsLaOpcionVerdadera()) {
            idDeLaRespuestaCorrecta = opcion.getId();
        }

        listaDeOpcionesDisponible.add(opcion);


    }


}
