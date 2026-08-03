package com.lorenzomar3.AQ.answering.infrastructure.controller.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.api.TipoAResponder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaDePreguntaDTO(Long idPregunta, TipoAResponder tipoDePregunta, String respuestaInput, Boolean respuestaBooleana, List<Opcion> listaDeOpciones, List<OpcionDeDesplegableCompartido> listaDeOpcionesParaDesplegableCompartidos, List<SeleccionUnicaParaDesplegableIndependiente> listaDeSeleccionesUnicasParaDesplegableIndependiente) {}
