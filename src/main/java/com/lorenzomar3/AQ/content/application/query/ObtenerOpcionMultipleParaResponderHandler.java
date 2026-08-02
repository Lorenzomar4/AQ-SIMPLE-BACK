package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.ObtenerOpcionMultipleParaResponderQuery;
import com.lorenzomar3.AQ.content.api.OpcionMultipleParaResponderView;
import com.lorenzomar3.AQ.content.api.OpcionView;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerOpcionMultipleParaResponderHandler implements ObtenerOpcionMultipleParaResponderQuery {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public ObtenerOpcionMultipleParaResponderHandler(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Override
    public Optional<OpcionMultipleParaResponderView> obtenerPorId(Long id) {
        return opcionMultipleRepositoryPort.findById(id)
                .map(pregunta -> new OpcionMultipleParaResponderView(
                        pregunta.getId(),
                        pregunta.getListaDeOpciones().stream()
                                .map(opcion -> new OpcionView(opcion.getId(), opcion.getLaRespuestaEs()))
                                .toList(),
                        pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
