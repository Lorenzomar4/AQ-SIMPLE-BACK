package com.lorenzomar3.AQ.content.application.command;

import com.lorenzomar3.AQ.content.api.ActualizarCriticoDePreguntaSimpleCommand;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class ActualizarCriticoDePreguntaSimpleHandler implements ActualizarCriticoDePreguntaSimpleCommand {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public ActualizarCriticoDePreguntaSimpleHandler(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    public void actualizar(Long id, Integer intentosParaQueDejeDeSerCriticoDisponible) {
        PreguntaSimple preguntaSimple = preguntaSimpleRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        preguntaSimple.setIntentosParaQueDejeDeSerCriticoDisponible(intentosParaQueDejeDeSerCriticoDisponible);
        preguntaSimpleRepositoryPort.save(preguntaSimple);
    }
}
