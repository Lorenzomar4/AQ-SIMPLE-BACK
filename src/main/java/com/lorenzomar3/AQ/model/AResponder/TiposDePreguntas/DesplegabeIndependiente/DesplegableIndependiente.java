package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.IPreguntaVariasOpciones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependiente extends Pregunta implements IPreguntaVariasOpciones<Long, SeleccionUnicaParaDesplegableIndependiente> {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente = new ArrayList<>();


    @PostLoad
    void init() {
        //Por alguna razon se traen los elementos de esta lista de manera duplicada desde la base de datos a pesar de que alli
        // Ejemplo con numeros [1,2,1,2] . Esto claramente yo no quiero asi que con esto logro la unicidad [1,2]
        listaDeOpcionDesplegableIndependiente = listaDeOpcionDesplegableIndependiente.stream().distinct().toList();
    }


    public DesplegableIndependiente(String titulo, List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente) {
        super(titulo);
        this.listaDeOpcionDesplegableIndependiente = listaDeOpcionDesplegableIndependiente;
    }


    @Override
    public void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpciones() {
        return new ArrayList<>(this.listaDeOpcionDesplegableIndependiente);
    }

    @Override
    public List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        return respuestaDePreguntaDTO.getListaDeSeleccionesUnicasParaDesplegableIndependiente();
    }

}
