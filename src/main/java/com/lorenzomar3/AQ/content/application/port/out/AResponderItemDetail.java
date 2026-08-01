package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;

public record AResponderItemDetail(Long id, TipoAResponder tipo, String titulo, LocalDateTime fechaDeCreacion,
                                    Boolean esCritico, Integer numeroDePreguntas) {
}
