package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerdaderoOFalsoParaResponder {

    private Long id;
    private Boolean respuestaVerdadera;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(boolean respuestaDelUsuario) {
        boolean esCorrecta = respuestaVerdadera == respuestaDelUsuario;
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
