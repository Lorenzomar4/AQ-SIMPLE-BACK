package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SeleccionUnica extends APreguntaVariasOpciones<Boolean,Opcion> {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn()
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpcionesConSuRespuestaReal = new ArrayList<>();


    @PostLoad
    void init() {
        Collections.shuffle(listaDeOpcionesConSuRespuestaReal);
    }

    public SeleccionUnica(String titulo, List<Opcion> listaDeOpcionesDisponible) {
        super(titulo);
        this.listaDeOpcionesConSuRespuestaReal = listaDeOpcionesDisponible;
    }

    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

    }

    @Override
    public List<Opcion> listaDeOpcionesConSuRespuestaReal() {
        return listaDeOpcionesConSuRespuestaReal;
    }

    @Override
    public List<Opcion> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeOpciones();
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
        int numeroDeOpcionesVerdaderas = lista.stream().filter(op -> op.getRespuestaCorrecta()).toList().size();

        return numeroDeOpcionesVerdaderas == 1;

    }

    public void agregarLaOpcionALaListaYAsignarLaIdDeLaOpcionCorrecta(Opcion opcion) {
        listaDeOpcionesConSuRespuestaReal.add(opcion);
    }


}
