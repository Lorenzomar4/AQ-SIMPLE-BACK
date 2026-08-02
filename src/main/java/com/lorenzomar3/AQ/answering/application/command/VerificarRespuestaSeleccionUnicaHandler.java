package com.lorenzomar3.AQ.answering.application.command;

import com.lorenzomar3.AQ.answering.domain.EstadoCritico;
import com.lorenzomar3.AQ.answering.domain.OpcionParaResponder;
import com.lorenzomar3.AQ.answering.domain.SeleccionUnicaParaResponder;
import com.lorenzomar3.AQ.content.api.ActualizarCriticoDeSeleccionUnicaCommand;
import com.lorenzomar3.AQ.content.api.ObtenerSeleccionUnicaParaResponderQuery;
import com.lorenzomar3.AQ.content.api.SeleccionUnicaParaResponderView;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VerificarRespuestaSeleccionUnicaHandler {

    private final ObtenerSeleccionUnicaParaResponderQuery obtenerSeleccionUnicaParaResponderQuery;
    private final ActualizarCriticoDeSeleccionUnicaCommand actualizarCriticoDeSeleccionUnicaCommand;

    public VerificarRespuestaSeleccionUnicaHandler(ObtenerSeleccionUnicaParaResponderQuery obtenerSeleccionUnicaParaResponderQuery,
                                                     ActualizarCriticoDeSeleccionUnicaCommand actualizarCriticoDeSeleccionUnicaCommand) {
        this.obtenerSeleccionUnicaParaResponderQuery = obtenerSeleccionUnicaParaResponderQuery;
        this.actualizarCriticoDeSeleccionUnicaCommand = actualizarCriticoDeSeleccionUnicaCommand;
    }

    public Boolean ejecutar(VerificarRespuestaSeleccionUnicaCommand command) {
        SeleccionUnicaParaResponderView view = obtenerSeleccionUnicaParaResponderQuery.obtenerPorId(command.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        SeleccionUnicaParaResponder pregunta = toDomain(view);
        List<OpcionParaResponder> opcionesDelUsuario = command.opcionesDelUsuario().stream()
                .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.marcada()))
                .toList();

        Boolean esCorrecta = pregunta.verificarRespuesta(opcionesDelUsuario);
        actualizarCriticoDeSeleccionUnicaCommand.actualizar(
                pregunta.getId(), pregunta.getEstadoCritico().getIntentosParaQueDejeDeSerCriticoDisponible());
        return esCorrecta;
    }

    private SeleccionUnicaParaResponder toDomain(SeleccionUnicaParaResponderView view) {
        EstadoCritico estadoCritico = new EstadoCritico();
        estadoCritico.setIntentosParaQueDejeDeSerCriticoDisponible(view.intentosParaQueDejeDeSerCriticoDisponible());

        SeleccionUnicaParaResponder pregunta = new SeleccionUnicaParaResponder();
        pregunta.setId(view.id());
        pregunta.setOpciones(view.listaDeOpciones().stream()
                .map(opcion -> new OpcionParaResponder(opcion.id(), opcion.laRespuestaEs()))
                .toList());
        pregunta.setEstadoCritico(estadoCritico);
        return pregunta;
    }
}
