package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Entity
@NoArgsConstructor
public class OpcionMultiple extends Pregunta implements IPreguntaVariasOpciones<Boolean, Opcion> {


    public OpcionMultiple(String titulo, List<Opcion> listaDeOpcionesConSuRespuestaReal) {
        super(titulo);
        this.listaDeOpcionesConSuRespuestaReal = listaDeOpcionesConSuRespuestaReal;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpcionesConSuRespuestaReal;


    public OpcionMultiple(String titulo) {
        super(titulo);
    }

    @PostLoad
    void init() {
        Collections.shuffle(listaDeOpcionesConSuRespuestaReal);
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


}
