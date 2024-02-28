package com.lorenzomar3.AQ.dto.dto;

import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@NoArgsConstructor
@Setter
public class RespuestaDePreguntaDTO {
    Long idPregunta;

    Boolean respuestaBooleana;

    List<Opcion> listaDeOpciones;

    List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos;

    List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente;

}
