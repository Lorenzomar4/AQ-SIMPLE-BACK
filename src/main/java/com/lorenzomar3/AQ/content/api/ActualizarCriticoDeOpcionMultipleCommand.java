package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDeOpcionMultipleCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
