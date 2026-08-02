package com.lorenzomar3.AQ.answering.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreguntaSimpleParaResponderTest {

    private PreguntaSimpleParaResponder nueva(int contador) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(contador);

        PreguntaSimpleParaResponder pregunta = new PreguntaSimpleParaResponder();
        pregunta.setId(1L);
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }

    @Test
    void verificarRespuestaDevuelveDirectamenteElBooleanoRecibidoSinCompararDatoAlmacenado() {
        PreguntaSimpleParaResponder pregunta = nueva(0);

        assertTrue(pregunta.verificarRespuesta(true));
        assertFalse(pregunta.verificarRespuesta(false));
    }

    @Test
    void verificarRespuestaVerdaderaActualizaElEstadoCriticoComoCorrecta() {
        PreguntaSimpleParaResponder pregunta = nueva(3);

        pregunta.verificarRespuesta(true);

        assertEquals(2, pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void verificarRespuestaFalsaActualizaElEstadoCriticoComoIncorrecta() {
        PreguntaSimpleParaResponder pregunta = nueva(0);

        pregunta.verificarRespuesta(false);

        assertEquals(3, pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
    }
}
