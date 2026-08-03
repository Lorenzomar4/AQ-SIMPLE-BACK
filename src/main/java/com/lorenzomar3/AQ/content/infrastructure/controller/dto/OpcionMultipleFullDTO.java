package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.time.LocalDateTime;
import java.util.List;

public record OpcionMultipleFullDTO(Long id, String titulo, String descripcion, Long idDuenio,
                                     LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion,
                                     TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible,
                                     String imagenTitulo, List<OpcionFullDTO> listaDeOpciones) {}
