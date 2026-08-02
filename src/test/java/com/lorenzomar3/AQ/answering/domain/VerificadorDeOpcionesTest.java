package com.lorenzomar3.AQ.answering.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerificadorDeOpcionesTest {

    private record OpcionDePrueba(Long id, Boolean valorCorrecto) implements OpcionVerificable<Boolean> {
        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Boolean getValorCorrecto() {
            return valorCorrecto;
        }
    }

    private final VerificadorDeOpciones<Boolean, OpcionDePrueba> verificador = new VerificadorDeOpciones<>();

    @Test
    void coincideCuandoElUsuarioMarcaLasMismasOpcionesQueLaRespuestaReal() {
        List<OpcionDePrueba> real = List.of(new OpcionDePrueba(1L, true), new OpcionDePrueba(2L, false));
        List<OpcionDePrueba> delUsuario = List.of(new OpcionDePrueba(1L, true), new OpcionDePrueba(2L, false));

        assertTrue(verificador.coincidenciaTotal(real, delUsuario));
    }

    @Test
    void noCoincideCuandoElUsuarioMarcaUnaOpcionDistinta() {
        List<OpcionDePrueba> real = List.of(new OpcionDePrueba(1L, true), new OpcionDePrueba(2L, false));
        List<OpcionDePrueba> delUsuario = List.of(new OpcionDePrueba(1L, false), new OpcionDePrueba(2L, false));

        assertFalse(verificador.coincidenciaTotal(real, delUsuario));
    }

    @Test
    void coincideParcialCuandoElUsuarioSoloMarcaAlgunasOpcionesYCoincidenLasQueMando() {
        List<OpcionDePrueba> real = List.of(new OpcionDePrueba(1L, true), new OpcionDePrueba(2L, false));
        List<OpcionDePrueba> delUsuario = List.of(new OpcionDePrueba(1L, true));

        assertTrue(verificador.coincidenciaTotal(real, delUsuario));
    }
}
