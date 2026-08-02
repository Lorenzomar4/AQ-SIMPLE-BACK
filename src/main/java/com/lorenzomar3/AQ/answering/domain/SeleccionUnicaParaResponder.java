package com.lorenzomar3.AQ.answering.domain;

import com.lorenzomar3.AQ.exception.BussinesException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SeleccionUnicaParaResponder {

    private Long id;
    private List<OpcionParaResponder> opciones;
    private EstadoCritico estadoCritico;

    private final VerificadorDeOpciones<Boolean, OpcionParaResponder> verificador = new VerificadorDeOpciones<>();

    public Boolean verificarRespuesta(List<OpcionParaResponder> opcionesDelUsuario) {
        validarUnaSolaOpcionMarcada(opcionesDelUsuario);

        boolean esCorrecta = verificador.coincidenciaTotal(opciones, opcionesDelUsuario);
        estadoCritico.actualizar(esCorrecta);
        return esCorrecta;
    }

    private void validarUnaSolaOpcionMarcada(List<OpcionParaResponder> opcionesDelUsuario) {
        long marcadas = opcionesDelUsuario.stream().filter(OpcionParaResponder::esCorrecta).count();
        if (marcadas != 1) {
            throw new BussinesException("¡Asegurese de que haya solamente una opcion valida!");
        }
    }
}
