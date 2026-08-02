package com.lorenzomar3.AQ.dto.newDto;

import java.util.List;

public record SeleccionUnicaParaDesplegableIndependienteFetchDTO(Long id, String titulo,
                                                                   List<OpcionFetchDTO> listaDeOpcionesDisponible) {}
