package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Opcion {

    private Long id;
    private String opcion;
    private Boolean laRespuestaEs;
}
