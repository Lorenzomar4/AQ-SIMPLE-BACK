package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import java.util.List;

public record SeleccionUnicaParaDesplegableIndependienteFullDTO(Long id, String titulo,
                                                                  List<OpcionFullDTO> listaDeOpcionesDisponible) {}
