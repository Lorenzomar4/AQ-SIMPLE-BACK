package com.lorenzomar3.AQ.dto.newDto;

import java.util.List;

public record SeleccionUnicaParaDesplegableIndependienteFullDTO(Long id, String titulo,
                                                                  List<OpcionFullDTO> listaDeOpcionesDisponible) {}
