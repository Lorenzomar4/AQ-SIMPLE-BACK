package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.AResponder.OpcionDeSeleccionUnica;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class SeleccionUnica extends Pregunta<Long> {


    Long idDeLaRespuestaCorrecta;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    public List<OpcionDeSeleccionUnica> listaDeOpcionesDisponible;


    @Override
    public boolean laRespuestaEsCorrecta(Long idRespuestaCorrecta) {
        return idDeLaRespuestaCorrecta.equals(idRespuestaCorrecta);
    }


    public void setListaDeOpcionesDisponible(List<OpcionDeSeleccionUnica> lista) {
        if (!existeUnaOpcionVerdaderaUnicamente(lista)) {
            throw new ErrorDeNegocio("¡Asegurese de que haya solamente una opcion valida!");
        }


        lista.forEach(op -> agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(op));

    }

    private Boolean existeUnaOpcionVerdaderaUnicamente(List<OpcionDeSeleccionUnica> lista) {
        int numeroDeOpcionesVerdaderas = lista.stream().filter(op -> op.getEsLaOpcionVerdadera()).toList().size();

        return numeroDeOpcionesVerdaderas == 1;

    }

    public void agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(OpcionDeSeleccionUnica opcionDeSeleccionUnica) {

        if (opcionDeSeleccionUnica.getEsLaOpcionVerdadera()) {
            idDeLaRespuestaCorrecta = opcionDeSeleccionUnica.getId();
        }

        listaDeOpcionesDisponible.add(opcionDeSeleccionUnica);


    }


}
