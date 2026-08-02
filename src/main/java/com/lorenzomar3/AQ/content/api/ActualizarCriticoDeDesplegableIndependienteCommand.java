package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDeDesplegableIndependienteCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
