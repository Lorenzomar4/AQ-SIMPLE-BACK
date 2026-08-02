package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.DesplegableCompartidoParaResponderView;
import com.lorenzomar3.AQ.content.api.ObtenerDesplegableCompartidoParaResponderQuery;
import com.lorenzomar3.AQ.content.api.OpcionDeDesplegableCompartidoView;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerDesplegableCompartidoParaResponderHandler implements ObtenerDesplegableCompartidoParaResponderQuery {

    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public ObtenerDesplegableCompartidoParaResponderHandler(DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Override
    public Optional<DesplegableCompartidoParaResponderView> obtenerPorId(Long id) {
        return desplegableCompartidoRepositoryPort.findById(id)
                .map(pregunta -> new DesplegableCompartidoParaResponderView(
                        pregunta.getId(),
                        pregunta.getListaDeOpciones().stream()
                                .map(opcion -> new OpcionDeDesplegableCompartidoView(opcion.getId(), opcion.getRespuesta()))
                                .toList(),
                        pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
