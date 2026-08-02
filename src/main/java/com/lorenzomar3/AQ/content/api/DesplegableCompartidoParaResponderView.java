package com.lorenzomar3.AQ.content.api;

import java.util.List;

public record DesplegableCompartidoParaResponderView(Long id, List<OpcionDeDesplegableCompartidoView> listaDeOpciones,
                                                       Integer intentosParaQueDejeDeSerCriticoDisponible) {
}
