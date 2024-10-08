package com.lorenzomar3.AQ.dto.newDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.TipoAResponder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@NoArgsConstructor
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespuestaDePreguntaDTO {
    Long idPregunta;

    TipoAResponder tipoDePregunta;

    String respuestaInput;

    Boolean respuestaBooleana;

    List<Opcion> listaDeOpciones;

    List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos;

    List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente;

    public RespuestaDePreguntaDTO(Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana, List<Opcion> listaDeOpciones, List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos, List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente) {
        this.idPregunta = idPregunta;
        this.tipoDePregunta = tipoDePregunta;
        this.respuestaInput = respuestaInput;
        this.respuestaBooleana = respuestaBooleana;
        this.listaDeOpciones = listaDeOpciones;
        this.listaDeOpcionesParaDesplegableCompartidos = listaDeOpcionesParaDesplegableCompartidos;
        this.listaDeSeleccionesUnicasParaDesplegableIndependiente = listaDeSeleccionesUnicasParaDesplegableIndependiente;
    }
}

