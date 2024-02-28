package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import lombok.Data;
import lombok.Getter;
//ES POSIBLE ELIMINAR ESTA CLASE?

@Getter
public class RespuestaDeDesplegableIndependiente {

    @Getter
    Long idDeLaPregunta;

    Long idDeLaRespuestaSeleccionada;


}
