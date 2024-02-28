package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
public class Verificador<T, G extends IRespuestaOpcion<T>> {

    HashMap<Long, T> keyIdPrguntaValueRespuestaCorrecta = new HashMap<>();

    public Boolean coincidenciaTotal( List<G> respuestaReal ,List<G> respuestaDelusuario) {
        setMap(respuestaReal);
        return respuestaDelusuario.stream().allMatch(this::determinarCoincidencia);
    }

    private void setMap(List<G> respuestaReal) {
        respuestaReal.forEach(op -> {
            keyIdPrguntaValueRespuestaCorrecta.put(op.getId(), op.getRespuestaCorrecta());
        });
    }

    private Boolean determinarCoincidencia(G respuestaDelusuario) {
        return keyIdPrguntaValueRespuestaCorrecta.get(respuestaDelusuario.getId()).equals(respuestaDelusuario.getRespuestaCorrecta());
    }


}
