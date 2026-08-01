package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;

public record PreguntaSimpleFullDTO(Long id, String titulo, String descripcion, Long idDuenio,
                                     LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion,
                                     TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible,
                                     String imagenTitulo, Boolean respuestaPrecisa, String respuestaEstablecida) {}
