package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeDesplegableCompartidoCommand;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDeDesplegableCompartidoHandler implements ActualizarCriticoDeDesplegableCompartidoCommand {

    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public ActualizarCriticoDeDesplegableCompartidoHandler(DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        DesplegableCompartido desplegableCompartido = desplegableCompartidoRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        desplegableCompartido.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        desplegableCompartidoRepositoryPort.save(desplegableCompartido);
    }
}
