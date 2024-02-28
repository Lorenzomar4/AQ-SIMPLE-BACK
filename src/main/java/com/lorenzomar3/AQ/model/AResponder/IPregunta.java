package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;

public interface IPregunta{
    boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuesta);

}
