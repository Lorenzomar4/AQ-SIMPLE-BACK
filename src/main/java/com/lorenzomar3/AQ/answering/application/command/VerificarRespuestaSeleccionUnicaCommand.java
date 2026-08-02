package com.lorenzomar3.AQ.answering.application.command;

import java.util.List;

public record VerificarRespuestaSeleccionUnicaCommand(Long idPregunta, List<OpcionRespuestaDTO> opcionesDelUsuario) {}
