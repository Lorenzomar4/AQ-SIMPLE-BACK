package com.lorenzomar3.AQ.content.api;

import java.util.List;

public record DesplegableIndependienteParaResponderView(Long id, List<SubPreguntaView> listaDeOpciones,
                                                          Integer intentosParaQueDejeDeSerCriticoDisponible) {
}
