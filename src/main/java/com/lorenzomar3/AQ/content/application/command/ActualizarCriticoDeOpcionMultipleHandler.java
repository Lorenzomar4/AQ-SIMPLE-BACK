package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeOpcionMultipleCommand;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDeOpcionMultipleHandler implements ActualizarCriticoDeOpcionMultipleCommand {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public ActualizarCriticoDeOpcionMultipleHandler(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        OpcionMultiple opcionMultiple = opcionMultipleRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        opcionMultiple.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        opcionMultipleRepositoryPort.save(opcionMultiple);
    }
}
