package com.lorenzomar3.AQ.answering.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadoCriticoTest {

    @Test
    void respuestaIncorrectaFijaElContadorEnTres() {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        estadoCritico.actualizar(false);

        assertEquals(3, estadoCritico.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void respuestaCorrectaDecrementaElContadorSiEsMayorQueCero() {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(3);

        estadoCritico.actualizar(true);

        assertEquals(2, estadoCritico.getIntentosParaQueDejeDeSerCriticoDisponible());
    }

    @Test
    void respuestaCorrectaConContadorEnCeroSeMantieneEnCero() {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        estadoCritico.actualizar(true);

        assertEquals(0, estadoCritico.getIntentosParaQueDejeDeSerCriticoDisponible());
    }
}
