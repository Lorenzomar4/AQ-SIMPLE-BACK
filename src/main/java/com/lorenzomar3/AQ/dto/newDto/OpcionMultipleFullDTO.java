package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;
import java.util.List;

public record OpcionMultipleFullDTO(Long id, String titulo, String descripcion, Long idDuenio,
                                     LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion,
                                     TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible,
                                     String imagenTitulo, List<OpcionFullDTO> listaDeOpciones) {}
