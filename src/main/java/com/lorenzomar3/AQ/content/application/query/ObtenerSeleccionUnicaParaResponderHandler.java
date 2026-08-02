package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.ObtenerSeleccionUnicaParaResponderQuery;
import com.lorenzomar3.AQ.content.api.OpcionView;
import com.lorenzomar3.AQ.content.api.SeleccionUnicaParaResponderView;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerSeleccionUnicaParaResponderHandler implements ObtenerSeleccionUnicaParaResponderQuery {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public ObtenerSeleccionUnicaParaResponderHandler(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Override
    public Optional<SeleccionUnicaParaResponderView> obtenerPorId(Long id) {
        return seleccionUnicaRepositoryPort.findById(id)
                .map(pregunta -> new SeleccionUnicaParaResponderView(
                        pregunta.getId(),
                        pregunta.getListaDeOpciones().stream()
                                .map(opcion -> new OpcionView(opcion.getId(), opcion.getLaRespuestaEs()))
                                .toList(),
                        pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
