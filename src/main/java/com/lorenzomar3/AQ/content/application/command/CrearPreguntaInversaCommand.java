package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

public record CrearPreguntaInversaCommand(Long idPregunta, TipoAResponder tipo) {}
