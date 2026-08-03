package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.time.LocalDateTime;

public record PreguntaSimpleFetchDTO(Long id, String titulo, String descripcion, Long idDuenio,
                                      LocalDateTime fechaDeCreacion, LocalDateTime ultimaActualizacion,
                                      TipoAResponder tipo, Integer intentosParaQueDejeDeSerCriticoDisponible,
                                      String imagenTitulo, Boolean respuestaPrecisa) {}
