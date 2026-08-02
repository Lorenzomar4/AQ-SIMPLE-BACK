package com.lorenzomar3.AQ.content.api;

import java.util.List;

public record SeleccionUnicaParaResponderView(Long id, List<OpcionView> listaDeOpciones,
                                               Integer intentosParaQueDejeDeSerCriticoDisponible) {
}
