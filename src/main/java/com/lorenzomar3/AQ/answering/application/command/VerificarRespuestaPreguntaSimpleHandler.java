package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.PreguntaSimpleParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDePreguntaSimpleCommand;
import com.lorenzomar3.AQ.content.api.ObtenerPreguntaSimpleParaResponderQuery;
import com.lorenzomar3.AQ.content.api.PreguntaSimpleParaResponderView;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class VerificarRespuestaPreguntaSimpleHandler {

    private final ObtenerPreguntaSimpleParaResponderQuery obtenerPreguntaSimpleParaResponderQuery;
    private final ActualizarCriticoDePreguntaSimpleCommand actualizarCriticoDePreguntaSimpleCommand;

    public VerificarRespuestaPreguntaSimpleHandler(ObtenerPreguntaSimpleParaResponderQuery obtenerPreguntaSimpleParaResponderQuery,
                                                     ActualizarCriticoDePreguntaSimpleCommand actualizarCriticoDePreguntaSimpleCommand) {
        this.obtenerPreguntaSimpleParaResponderQuery = obtenerPreguntaSimpleParaResponderQuery;
        this.actualizarCriticoDePreguntaSimpleCommand = actualizarCriticoDePreguntaSimpleCommand;
    }

    public Boolean ejecutar(VerificarRespuestaPreguntaSimpleCommand command) {
        PreguntaSimpleParaResponderView view = obtenerPreguntaSimpleParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        PreguntaSimpleParaResponder pregunta = toDomain(view);
        Boolean esCorrecta = pregunta.verificarRespuesta(command.respuestaBooleana());
        actualizarCriticoDePreguntaSimpleCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private PreguntaSimpleParaResponder toDomain(PreguntaSimpleParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        PreguntaSimpleParaResponder pregunta = new PreguntaSimpleParaResponder();
        pregunta.setId(view.id());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
