package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.VerdaderoOFalsoParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeVerdaderoOFalsoCommand;
import com.lorenzomar3.AQ.content.api.ObtenerVerdaderoOFalsoParaResponderQuery;
import com.lorenzomar3.AQ.content.api.VerdaderoOFalsoParaResponderView;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class VerificarRespuestaVerdaderoOFalsoHandler {

    private final ObtenerVerdaderoOFalsoParaResponderQuery obtenerVerdaderoOFalsoParaResponderQuery;
    private final ActualizarCriticoDeVerdaderoOFalsoCommand actualizarCriticoDeVerdaderoOFalsoCommand;

    public VerificarRespuestaVerdaderoOFalsoHandler(ObtenerVerdaderoOFalsoParaResponderQuery obtenerVerdaderoOFalsoParaResponderQuery,
                                                      ActualizarCriticoDeVerdaderoOFalsoCommand actualizarCriticoDeVerdaderoOFalsoCommand) {
        this.obtenerVerdaderoOFalsoParaResponderQuery = obtenerVerdaderoOFalsoParaResponderQuery;
        this.actualizarCriticoDeVerdaderoOFalsoCommand = actualizarCriticoDeVerdaderoOFalsoCommand;
    }

    public Boolean ejecutar(VerificarRespuestaVerdaderoOFalsoCommand command) {
        VerdaderoOFalsoParaResponderView view = obtenerVerdaderoOFalsoParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        VerdaderoOFalsoParaResponder pregunta = toDomain(view);
        Boolean esCorrecta = pregunta.verificarRespuesta(command.respuestaBooleana());
        actualizarCriticoDeVerdaderoOFalsoCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private VerdaderoOFalsoParaResponder toDomain(VerdaderoOFalsoParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        VerdaderoOFalsoParaResponder pregunta = new VerdaderoOFalsoParaResponder();
        pregunta.setId(view.id());
        pregunta.setRespuestaVerdadera(view.respuestaVerdadera());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
