package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OpcionMultipleParaResponder {

    private Long id;
    private List<OpcionParaResponder> opciones;
    private EstadoCritico estadoCritico;

    private final VerificadorDeOpciones<Boolean, OpcionParaResponder> verificador = new VerificadorDeOpciones<>();

    public Boolean verificarRespuesta(List<OpcionParaResponder> opcionesDelUsuario) {
        boolean esCorrecta = verificador.coincidenciaTotal(opciones, opcionesDelUsuario);
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
