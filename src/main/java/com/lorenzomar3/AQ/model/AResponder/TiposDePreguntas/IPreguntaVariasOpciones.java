package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.IPregunta;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.Verificador;

import java.util.List;


public interface IPreguntaVariasOpciones<T, G extends IRespuestaOpcion<T>> extends IPregunta {

    default boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {
        List<G> lista = listaDeOpcionesConLaRespuestaDelUsuario(respuestaDePreguntaDTO);

        validacionDeDatosDTO(respuestaDePreguntaDTO);
        Verificador<T, G> verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDeOpcionesConSuRespuestaReal(), lista);
    }


    void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO);

    List<G> listaDeOpcionesConSuRespuestaReal();

    List<G> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO);


}

//T - value , G extend IRespuestaOpcion<T>

//Alternativa con clase abstracta desechada

/*
@NoArgsConstructor
public abstract class APreguntaVariasOpciones<T, G extends IRespuestaOpcion<T>> extends Pregunta {





    public boolean laRespuestaEsCorrecta(RespuestaDePreguntaDTO respuestaDePreguntaDTO) {

        List<G> lista = listaDeOpcionesConLaRespuestaDelUsuario(respuestaDePreguntaDTO);

        validacionDeDatosDTO(respuestaDePreguntaDTO);
        Verificador<T, G> verificador = new Verificador<>();
        return verificador.coincidenciaTotal(listaDeOpcionesConSuRespuestaReal(), lista);
    }

    public abstract void validacionDeDatosDTO(RespuestaDePreguntaDTO respuestaDePreguntaDTO);

    public abstract List<G> listaDeOpcionesConSuRespuestaReal();

    public abstract List<G> listaDeOpcionesConLaRespuestaDelUsuario(RespuestaDePreguntaDTO respuestaDePreguntaDTO);


}
*/
