package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.IPreguntaVariasOpciones;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.Verificador;
import com.lorenzomar3.AQ.model.View;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependiente extends Pregunta implements IPreguntaVariasOpciones<Long, SeleccionUnicaParaDesplegableIndependiente> {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    @JsonView(View.JustToAnswer.class)
    List<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas;


    @PostLoad
    void init() {
        Collections.shuffle(listaDePreguntasConOpcionUnicas);
    }

    public DesplegableIndependiente(String titulo, List<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas) {
        super(titulo);
        this.listaDePreguntasConOpcionUnicas = listaDePreguntasConOpcionUnicas;
    }


    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionesConSuRespuestaReal() {
        return listaDePreguntasConOpcionUnicas;
    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeSeleccionesUnicasParaDesplegableIndependiente();
    }

}
