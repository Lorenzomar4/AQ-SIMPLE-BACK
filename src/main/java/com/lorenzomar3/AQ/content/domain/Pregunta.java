package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class Pregunta extends AResponder {

    private Integer intentosParaQueDejeDeSerCriticoDisponible;
    private String imagenTitulo;
}
