package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.DesplegableIndependienteParaResponderView;
import com.lorenzomar3.AQ.content.api.ObtenerDesplegableIndependienteParaResponderQuery;
import com.lorenzomar3.AQ.content.api.OpcionView;
import com.lorenzomar3.AQ.content.api.SubPreguntaView;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerDesplegableIndependienteParaResponderHandler implements ObtenerDesplegableIndependienteParaResponderQuery {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public ObtenerDesplegableIndependienteParaResponderHandler(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Override
    public Optional<DesplegableIndependienteParaResponderView> obtenerPorId(Long id) {
        return desplegableIndependienteRepositoryPort.findById(id)
                .map(pregunta -> new DesplegableIndependienteParaResponderView(
                        pregunta.getId(),
                        pregunta.getListaDeOpciones().stream()
                                .map(subPregunta -> new SubPreguntaView(
                                        subPregunta.getId(),
                                        subPregunta.getListaDeOpciones().stream()
                                                .map(opcion -> new OpcionView(opcion.getId(), opcion.getLaRespuestaEs()))
                                                .toList()))
                                .toList(),
                        pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
