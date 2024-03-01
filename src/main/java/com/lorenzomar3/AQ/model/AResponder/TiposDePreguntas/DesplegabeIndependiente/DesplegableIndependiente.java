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

import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependiente extends Pregunta implements IPreguntaVariasOpciones<Long, SeleccionUnicaParaDesplegableIndependiente> {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    Set<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas;




    public DesplegableIndependiente(String titulo, List<SeleccionUnicaParaDesplegableIndependiente> listaDePreguntasConOpcionUnicas) {
        super(titulo);
        this.listaDePreguntasConOpcionUnicas =  new HashSet<>(listaDePreguntasConOpcionUnicas);

    }


    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpciones() {
        return new ArrayList<>(this.listaDePreguntasConOpcionUnicas );
    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeSeleccionesUnicasParaDesplegableIndependiente();
    }

}
