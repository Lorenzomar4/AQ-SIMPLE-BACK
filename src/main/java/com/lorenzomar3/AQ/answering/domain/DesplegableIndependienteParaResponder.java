package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependienteParaResponder {

    private Long id;
    private List<SubPreguntaParaResponder> subPreguntas;
    private EstadoCritico estadoCritico;

    private final VerificadorDeOpciones<Long, SubPreguntaParaResponder> verificador = new VerificadorDeOpciones<>();

    public Boolean verificarRespuesta(List<SubPreguntaParaResponder> subPreguntasDelUsuario) {
        boolean esCorrecta = verificador.coincidenciaTotal(subPreguntas, subPreguntasDelUsuario);
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
