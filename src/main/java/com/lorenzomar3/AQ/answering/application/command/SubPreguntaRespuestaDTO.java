package com.lorenzomar3.AQ.answering.application.command;

import java.util.List;

public record SubPreguntaRespuestaDTO(Long id, List<OpcionRespuestaDTO> opciones) {}
