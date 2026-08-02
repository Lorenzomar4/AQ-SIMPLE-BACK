package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PreguntaSimpleParaResponder {

    private Long id;
    private EstadoCritico estadoCritico;

    public Boolean verificarRespuesta(boolean respuestaDelUsuario) {
        // comportamiento incompleto preservado tal cual: no compara contra ningún dato almacenado
        estadoCritico.actualizar(respuestaDelUsuario);
        return respuestaDelUsuario;
    }
}
