package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.AResponder.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.TipoAResponder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Builder
@Getter
@Setter
public class PostPreguntaDTO {

    Long id;
    String titulo;

    String descripcion;
    TipoAResponder tipo;

    //para preguntaVF
    Boolean respuestaVerdadera;

    //para pregunta simple
    String respuestaEstablecida;

    //Para todas las preguntas que lo tengan
    List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta = new ArrayList<>();

    //Para seleccion unica y multiple opcion
    List<Opcion> listaDeOpcionesConSuRespuestaReal;

    //Para Desplegable compartido
    List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido;

    //Para desplegable independiente
    List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente;


    public PostPreguntaDTO(Long id, String titulo, String descripcion, TipoAResponder tipo, Boolean respuestaVerdadera, String respuestaEstablecida, List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta, List<Opcion> listaDeOpcionesConSuRespuestaReal, List<OpcionDeDesplegableCompartido> listaDeOpcionDesplegableCompartido, List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpcionDesplegableIndependiente) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.respuestaVerdadera = respuestaVerdadera;
        this.respuestaEstablecida = respuestaEstablecida;
        this.listaDeTeoriaDeLaPregunta = listaDeTeoriaDeLaPregunta;
        this.listaDeOpcionesConSuRespuestaReal = listaDeOpcionesConSuRespuestaReal;
        this.listaDeOpcionDesplegableCompartido = listaDeOpcionDesplegableCompartido;
        this.listaDeOpcionDesplegableIndependiente = listaDeOpcionDesplegableIndependiente;
    }
}
