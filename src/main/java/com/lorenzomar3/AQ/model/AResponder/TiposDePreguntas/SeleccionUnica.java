package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
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
public class SeleccionUnica extends Pregunta<RespuestaDePreguntaDTO> {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpcionesDisponible = new ArrayList<>();


    public SeleccionUnica(String titulo, List<Opcion> listaDeOpcionesDisponible) {
        super(titulo);
        setListaDeOpcionesDisponible(listaDeOpcionesDisponible);
        this.listaDeOpcionesDisponible = listaDeOpcionesDisponible;
    }

    @Override
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        Opcion correcta = obtenerLaOpcionQueSeIndiqueVerdadera(listaDeOpcionesDisponible);
        Opcion queElUsuarioConsideraCorrecta = obtenerLaOpcionQueSeIndiqueVerdadera(respuestaDePreguntaDTO.getListaDeOpciones());

        return  correcta.getId().equals(queElUsuarioConsideraCorrecta.getId());
    }


    private Opcion filtrarLaOpcionCorrecta(List<Opcion> lista){
        return lista.stream().filter(Opcion::getEsLaOpcionVerdadera).toList().get(0);
    }

    public Opcion obtenerLaOpcionQueSeIndiqueVerdadera(List<Opcion> lista){
        validacionDeOpcionUnica(lista);

        return filtrarLaOpcionCorrecta(lista);

    }

    public void setListaDeOpcionesDisponible(List<Opcion> lista) {

        validacionDeOpcionUnica(lista);
        lista.forEach(op -> agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(op));

    }

    public void validacionDeOpcionUnica(List<Opcion> lista) {
        if (!existeUnaOpcionVerdaderaUnicamente(lista)) {
            throw new BussinesException("¡Asegurese de que haya solamente una opcion valida!");
        }
    }

    private Boolean existeUnaOpcionVerdaderaUnicamente(List<Opcion> lista) {
        int numeroDeOpcionesVerdaderas = lista.stream().filter(op -> op.getEsLaOpcionVerdadera()).toList().size();

        return numeroDeOpcionesVerdaderas == 1;

    }

    public void agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(Opcion opcion) {

        listaDeOpcionesDisponible.add(opcion);


    }


}
