package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.OpcionMultipleParaResponder;
import com.lorenzomar3.AQ.answering.domain.OpcionParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeOpcionMultipleCommand;
import com.lorenzomar3.AQ.content.api.ObtenerOpcionMultipleParaResponderQuery;
import com.lorenzomar3.AQ.content.api.OpcionMultipleParaResponderView;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificarRespuestaOpcionMultipleHandler {

    private final ObtenerOpcionMultipleParaResponderQuery obtenerOpcionMultipleParaResponderQuery;
    private final ActualizarCriticoDeOpcionMultipleCommand actualizarCriticoDeOpcionMultipleCommand;

    public VerificarRespuestaOpcionMultipleHandler(ObtenerOpcionMultipleParaResponderQuery obtenerOpcionMultipleParaResponderQuery,
                                                     ActualizarCriticoDeOpcionMultipleCommand actualizarCriticoDeOpcionMultipleCommand) {
        this.obtenerOpcionMultipleParaResponderQuery = obtenerOpcionMultipleParaResponderQuery;
        this.actualizarCriticoDeOpcionMultipleCommand = actualizarCriticoDeOpcionMultipleCommand;
    }

    public Boolean ejecutar(VerificarRespuestaOpcionMultipleCommand command) {
        OpcionMultipleParaResponderView view = obtenerOpcionMultipleParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        OpcionMultipleParaResponder pregunta = toDomain(view);
        List<OpcionParaResponder> opcionesDelUsuario = command.opcionesDelUsuario().stream()
                .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.marcada()))
                .toList();

        Boolean esCorrecta = pregunta.verificarRespuesta(opcionesDelUsuario);
        actualizarCriticoDeOpcionMultipleCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private OpcionMultipleParaResponder toDomain(OpcionMultipleParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        OpcionMultipleParaResponder pregunta = new OpcionMultipleParaResponder();
        pregunta.setId(view.id());
        pregunta.setOpciones(view.listaDeOpciones().stream()
                .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.laRespuestaEs()))
                .toList());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
