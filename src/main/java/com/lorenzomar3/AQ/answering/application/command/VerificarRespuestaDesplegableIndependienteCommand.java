package com.lorenzomar3.AQ.answering.application.command;

import java.util.List;

public record VerificarRespuestaDesplegableIndependienteCommand(Long idPregunta,
                                                                  List<SubPreguntaRespuestaDTO> subPreguntasDelUsuario) {}
