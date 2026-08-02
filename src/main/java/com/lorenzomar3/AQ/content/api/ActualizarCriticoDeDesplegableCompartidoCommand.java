package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDeDesplegableCompartidoCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
