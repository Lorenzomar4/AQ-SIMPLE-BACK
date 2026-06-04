package com.lorenzomar3.AQ.dto.newDto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.TipoAResponder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaDePreguntaDTO(Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana, List<Opcion> listaDeOpciones, List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos, List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente) {}