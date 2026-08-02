package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDeSeleccionUnicaCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
