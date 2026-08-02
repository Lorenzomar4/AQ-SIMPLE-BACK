package com.lorenzomar3.AQ.content.api;

public interface ActualizarCriticoDePreguntaSimpleCommand {

    void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible);
}
