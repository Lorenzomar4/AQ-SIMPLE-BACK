package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class OpcionMultiple extends Pregunta implements IPreguntaVariasOpciones<Boolean, Opcion> {


    public OpcionMultiple(String titulo, List<Opcion> listaDeOpcionesConSuRespuestaReal) {
        super(titulo);
        this.listaDeOpcionesConSuRespuestaReal = listaDeOpcionesConSuRespuestaReal;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
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
    public List<Opcion> listaDeOpciones() {
        return listaDeOpcionesConSuRespuestaReal;
    }

    @Override
    public List<Opcion> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeOpciones();
    }


}
