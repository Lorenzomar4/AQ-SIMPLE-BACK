package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeSeleccionUnicaCommand;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDeSeleccionUnicaHandler implements ActualizarCriticoDeSeleccionUnicaCommand {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public ActualizarCriticoDeSeleccionUnicaHandler(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        SeleccionUnica seleccionUnica = seleccionUnicaRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        seleccionUnica.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        seleccionUnicaRepositoryPort.save(seleccionUnica);
    }
}
