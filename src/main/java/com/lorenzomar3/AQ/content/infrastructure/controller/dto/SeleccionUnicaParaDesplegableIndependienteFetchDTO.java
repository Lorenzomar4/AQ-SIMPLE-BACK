package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import java.util.List;

public record SeleccionUnicaParaDesplegableIndependienteFetchDTO(Long id, String titulo,
                                                                   List<OpcionFetchDTO> listaDeOpcionesDisponible) {}
