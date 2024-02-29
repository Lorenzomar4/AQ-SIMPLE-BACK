package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.dto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.Verificador;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


//T - value , G extend IRespuestaOpcion<T>
@NoArgsConstructor
public abstract class APreguntaVariasOpciones<T, G extends IRespuestaOpcion<T>> extends Pregunta {



    public APreguntaVariasOpciones(String titulo) {
        super(titulo);
    }

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

interface IPreguntaVariasOpciones<T, G extends IRespuestaOpcion<T>> {

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
