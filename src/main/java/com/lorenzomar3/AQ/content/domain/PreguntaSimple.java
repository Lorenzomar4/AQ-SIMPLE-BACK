package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreguntaSimple extends Pregunta {

    private String respuestaEstablecida;
    private Boolean respuestaPrecisa;
}
