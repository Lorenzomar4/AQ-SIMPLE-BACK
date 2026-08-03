package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.TipoAResponder;
import java.util.ArrayList;
import java.util.List;

public record PostPreguntaDTO(Long id, String titulo, String descripcion, TipoAResponder tipo, Long idTemarioPerteneciente, Boolean respuestaVerdadera, String respuestaEstablecida, List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta, List<Opcion> listaDeOpcionesConSuRespuestaReal, List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido, List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente) {
    // Constructor requerido para inicializar la lista de forma explícita si el record lo requiere.
}