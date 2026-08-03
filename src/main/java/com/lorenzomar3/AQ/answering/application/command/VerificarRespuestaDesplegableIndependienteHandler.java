package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.DesplegableIndependienteParaResponder;
import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.OpcionParaResponder;
import com.lorenzomar3.AQ.answering.domain.SubPreguntaParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeDesplegableIndependienteCommand;
import com.lorenzomar3.AQ.content.api.DesplegableIndependienteParaResponderView;
import com.lorenzomar3.AQ.content.api.ObtenerDesplegableIndependienteParaResponderQuery;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificarRespuestaDesplegableIndependienteHandler {

    private final ObtenerDesplegableIndependienteParaResponderQuery obtenerDesplegableIndependienteParaResponderQuery;
    private final ActualizarCriticoDeDesplegableIndependienteCommand actualizarCriticoDeDesplegableIndependienteCommand;

    public VerificarRespuestaDesplegableIndependienteHandler(ObtenerDesplegableIndependienteParaResponderQuery obtenerDesplegableIndependienteParaResponderQuery,
                                                               ActualizarCriticoDeDesplegableIndependienteCommand actualizarCriticoDeDesplegableIndependienteCommand) {
        this.obtenerDesplegableIndependienteParaResponderQuery = obtenerDesplegableIndependienteParaResponderQuery;
        this.actualizarCriticoDeDesplegableIndependienteCommand = actualizarCriticoDeDesplegableIndependienteCommand;
    }

    public Boolean ejecutar(VerificarRespuestaDesplegableIndependienteCommand command) {
        DesplegableIndependienteParaResponderView view = obtenerDesplegableIndependienteParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        DesplegableIndependienteParaResponder pregunta = toDomain(view);
        List<SubPreguntaParaResponder> subPreguntasDelUsuario = command.subPreguntasDelUsuario().stream()
                .map(this::toDomain)
                .toList();

        Boolean esCorrecta = pregunta.verificarRespuesta(subPreguntasDelUsuario);
        actualizarCriticoDeDesplegableIndependienteCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private SubPreguntaParaResponder toDomain(SubPreguntaRespuestaDTO subPregunta) {
        return new SubPreguntaParaResponder(subPregunta.id(), subPregunta.opciones().stream()
                .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.marcada()))
                .toList());
    }

    private DesplegableIndependienteParaResponder toDomain(DesplegableIndependienteParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        DesplegableIndependienteParaResponder pregunta = new DesplegableIndependienteParaResponder();
        pregunta.setId(view.id());
        pregunta.setSubPreguntas(view.listaDeOpciones().stream()
                .map(subPregunta -> new SubPreguntaParaResponder(subPregunta.id(), subPregunta.opciones().stream()
                        .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.laRespuestaEs()))
                        .toList()))
                .toList());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
