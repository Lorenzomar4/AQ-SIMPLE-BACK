package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeDesplegableIndependienteCommand;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDeDesplegableIndependienteHandler implements ActualizarCriticoDeDesplegableIndependienteCommand {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public ActualizarCriticoDeDesplegableIndependienteHandler(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        DesplegableIndependiente desplegableIndependiente = desplegableIndependienteRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        desplegableIndependiente.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        desplegableIndependienteRepositoryPort.save(desplegableIndependiente);
    }
}
