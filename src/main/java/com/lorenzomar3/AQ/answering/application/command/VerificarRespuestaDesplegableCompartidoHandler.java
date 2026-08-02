package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.DesplegableCompartidoParaResponder;
import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.OpcionDeDesplegableCompartidoParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeDesplegableCompartidoCommand;
import com.lorenzomar3.AQ.content.api.DesplegableCompartidoParaResponderView;
import com.lorenzomar3.AQ.content.api.ObtenerDesplegableCompartidoParaResponderQuery;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificarRespuestaDesplegableCompartidoHandler {

    private final ObtenerDesplegableCompartidoParaResponderQuery obtenerDesplegableCompartidoParaResponderQuery;
    private final ActualizarCriticoDeDesplegableCompartidoCommand actualizarCriticoDeDesplegableCompartidoCommand;

    public VerificarRespuestaDesplegableCompartidoHandler(ObtenerDesplegableCompartidoParaResponderQuery obtenerDesplegableCompartidoParaResponderQuery,
                                                            ActualizarCriticoDeDesplegableCompartidoCommand actualizarCriticoDeDesplegableCompartidoCommand) {
        this.obtenerDesplegableCompartidoParaResponderQuery = obtenerDesplegableCompartidoParaResponderQuery;
        this.actualizarCriticoDeDesplegableCompartidoCommand = actualizarCriticoDeDesplegableCompartidoCommand;
    }

    public Boolean ejecutar(VerificarRespuestaDesplegableCompartidoCommand command) {
        DesplegableCompartidoParaResponderView view = obtenerDesplegableCompartidoParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        DesplegableCompartidoParaResponder pregunta = toDomain(view);
        List<OpcionDeDesplegableCompartidoParaResponder> opcionesDelUsuario = command.opcionesDelUsuario().stream()
                .map(opcion -> new OpcionDeDesplegableCompartidoParaResponder(opcion.id(), opcion.respuesta()))
                .toList();

        Boolean esCorrecta = pregunta.verificarRespuesta(opcionesDelUsuario);
        actualizarCriticoDeDesplegableCompartidoCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private DesplegableCompartidoParaResponder toDomain(DesplegableCompartidoParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        DesplegableCompartidoParaResponder pregunta = new DesplegableCompartidoParaResponder();
        pregunta.setId(view.id());
        pregunta.setOpciones(view.listaDeOpciones().stream()
                .map(opcion -> new OpcionDeDesplegableCompartidoParaResponder(opcion.id(), opcion.respuesta()))
                .toList());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
