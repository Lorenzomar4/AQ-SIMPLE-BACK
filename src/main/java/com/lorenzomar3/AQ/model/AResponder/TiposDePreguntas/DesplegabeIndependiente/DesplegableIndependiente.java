package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;

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
public class DesplegableIndependiente extends Pregunta {


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
    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        Verificador<Long, SeleccionUnicaParaDesplegableIndependiente> verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDePreguntasConOpcionUnicas,
                respuestaDePreguntaDTO.getListaDeSeleccionesUnicasParaDesplegableIndependiente());

    }

}
