package com.lorenzomar3.AQ.answering.application.command;

import java.util.List;

public record VerificarRespuestaDesplegableCompartidoCommand(Long idPregunta,
                                                               List<OpcionDeDesplegableCompartidoRespuestaDTO> opcionesDelUsuario) {}
