package com.lorenzomar3.AQ.answering.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DesplegableCompartidoParaResponder {

    private Long id;
    private List<OpcionDeDesplegableCompartidoParaResponder> opciones;
    private EstadoCritico estadoCritico;

    private final VerificadorDeOpciones<String, OpcionDeDesplegableCompartidoParaResponder> verificador = new VerificadorDeOpciones<>();

    public Boolean verificarRespuesta(List<OpcionDeDesplegableCompartidoParaResponder> opcionesDelUsuario) {
        boolean esCorrecta = verificador.coincidenciaTotal(opciones, opcionesDelUsuario);
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }
}
