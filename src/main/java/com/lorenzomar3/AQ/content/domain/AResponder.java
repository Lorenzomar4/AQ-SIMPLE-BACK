package com.lorenzomar3.AQ.content.domain;

import com.lorenzomar3.AQ.model.TipoAResponder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public abstract class AResponder {

    private Long id;
    private String titulo;
    private String descripcion;
    private Long idDuenio;
    private LocalDateTime fechaDeCreacion;
    private TipoAResponder tipo;
}
