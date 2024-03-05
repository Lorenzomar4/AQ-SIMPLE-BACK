package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
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
public class SeleccionUnica extends Pregunta implements IPreguntaVariasOpciones<Boolean, Opcion> {

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_seleccion_unica")
    public List<Opcion> listaDeOpcionesConSuRespuestaReal = new ArrayList<>();


    @PostLoad
    void init() {
        Collections.shuffle(listaDeOpcionesConSuRespuestaReal);
    }

    public SeleccionUnica(String titulo, List<Opcion> listaDeOpcionesConSuRespuestaReal) {
        super(titulo);
        this.listaDeOpcionesConSuRespuestaReal = listaDeOpcionesConSuRespuestaReal;
    }

    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        validacionDeOpcionUnica(respuestaDePreguntaDTO.getListaDeOpciones());

    }

    @Override
    public List<Opcion> listaDeOpciones() {
        return listaDeOpcionesConSuRespuestaReal;
    }

    @Override
    public List<Opcion> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeOpciones();
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
