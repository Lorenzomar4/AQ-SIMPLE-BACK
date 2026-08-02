package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDeVerdaderoOFalsoCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
