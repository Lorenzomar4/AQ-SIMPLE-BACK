package com.lorenzomar3.AQ.answering.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerificadorDeOpciones<T, O extends OpcionVerificable<T>> {

    public boolean coincidenciaTotal(List<O> opcionesReales, List<O> opcionesDelUsuario) {
        Map<Long, T> valorCorrectoPorId = new HashMap<>();
        opcionesReales.forEach(opcion -> valorCorrectoPorId.put(opcion.getId(), opcion.getValorCorrecto()));

        return opcionesDelUsuario.stream()
                .allMatch(opcion -> valorCorrectoPorId.get(opcion.getId()).equals(opcion.getValorCorrecto()));
    }
}
