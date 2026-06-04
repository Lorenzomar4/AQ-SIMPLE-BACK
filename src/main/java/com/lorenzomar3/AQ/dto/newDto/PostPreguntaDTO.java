package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.AResponder.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import java.util.ArrayList;
import java.util.List;

public record PostPreguntaDTO(Long id, String titulo, String descripcion, TipoAResponder tipo, Long idTemarioPerteneciente, Boolean respuestaVerdadera, String respuestaEstablecida, List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta, List<Opcion> listaDeOpcionesConSuRespuestaReal, List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido, List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente) {
    // Constructor requerido para inicializar la lista de forma explícita si el record lo requiere.
}