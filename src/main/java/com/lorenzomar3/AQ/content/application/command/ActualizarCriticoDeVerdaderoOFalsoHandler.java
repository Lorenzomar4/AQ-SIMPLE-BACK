package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeVerdaderoOFalsoCommand;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDeVerdaderoOFalsoHandler implements ActualizarCriticoDeVerdaderoOFalsoCommand {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public ActualizarCriticoDeVerdaderoOFalsoHandler(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        VerdaderoOFalso verdaderoOFalso = verdaderoOFalsoRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        verdaderoOFalso.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        verdaderoOFalsoRepositoryPort.save(verdaderoOFalso);
    }
}
