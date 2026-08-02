package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EstadoCritico {

    private Integer intentosParaQueDejeDeSerCriticoDisponible;

    public void actualizar(boolean respuestaCorrecta) {
        if (!respuestaCorrecta) {
            intentosParaQueDejeDeSerCriticoDisponible = 3;
        } else if (intentosParaQueDejeDeSerCriticoDisponible != 0) {
            intentosParaQueDejeDeSerCriticoDisponible--;
        }
    }
}
