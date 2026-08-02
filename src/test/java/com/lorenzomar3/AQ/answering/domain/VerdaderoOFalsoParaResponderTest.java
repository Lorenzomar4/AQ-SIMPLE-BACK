package com.lorenzomar3.AQ.answering.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VerdaderoOFalsoParaResponderTest {

    private VerdaderoOFalsoParaResponder nueva(boolean respuestaVerdadera, int contador) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(contador);

        VerdaderoOFalsoParaResponder pregunta = new VerdaderoOFalsoParaResponder();
        pregunta.setId(1L);
        pregunta.setRespuestaVerdadera(respuestaVerdadera);
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }

    @Test
    void verificarRespuestaEsCorrectaCuandoCoincideConLaRespuestaVerdadera() {
        assertTrue(nueva(true, 0).verificarRespuesta(true));
        assertTrue(nueva(false, 0).verificarRespuesta(false));
    }

    @Test
    void verificarRespuestaEsIncorrectaCuandoNoCoincideConLaRespuestaVerdadera() {
        assertFalse(nueva(true, 0).verificarRespuesta(false));
        assertFalse(nueva(false, 0).verificarRespuesta(true));
    }

    @Test
    void verificarRespuestaCorrectaDecrementaElEstadoCritico() {
        VerdaderoOFalsoParaResponder pregunta = nueva(true, 3);

        pregunta.verificarRespuesta(true);

        assertEquals(2, pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void verificarRespuestaIncorrectaFijaElEstadoCriticoEnTres() {
        VerdaderoOFalsoParaResponder pregunta = nueva(true, 0);

        pregunta.verificarRespuesta(false);

        assertEquals(3, pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
    }
}
