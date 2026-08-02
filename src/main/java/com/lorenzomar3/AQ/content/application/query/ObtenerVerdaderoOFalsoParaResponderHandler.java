package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.ObtenerVerdaderoOFalsoParaResponderQuery;
import com.lorenzomar3.AQ.content.api.VerdaderoOFalsoParaResponderView;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerVerdaderoOFalsoParaResponderHandler implements ObtenerVerdaderoOFalsoParaResponderQuery {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public ObtenerVerdaderoOFalsoParaResponderHandler(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    public Optional<VerdaderoOFalsoParaResponderView> obtenerPorId(Long id) {
        return verdaderoOFalsoRepositoryPort.findById(id)
                .map(pregunta -> new VerdaderoOFalsoParaResponderView(
                        pregunta.getId(), pregunta.getRespuestaVerdadera(),
                        pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
