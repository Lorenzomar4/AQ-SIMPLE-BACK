package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.api.ObtenerPreguntaSimpleParaResponderQuery;
import com.lorenzomar3.AQ.content.api.PreguntaSimpleParaResponderView;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ObtenerPreguntaSimpleParaResponderHandler implements ObtenerPreguntaSimpleParaResponderQuery {

    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;

    public ObtenerPreguntaSimpleParaResponderHandler(PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort) {
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
    }

    @Override
    public Optional<PreguntaSimpleParaResponderView> obtenerPorId(Long id) {
        return preguntaSimpleRepositoryPort.findById(id)
                .map(pregunta -> new PreguntaSimpleParaResponderView(
                        pregunta.getId(), pregunta.getIntentosParaQueDejeDeSerCriticoDisponible()));
    }
}
