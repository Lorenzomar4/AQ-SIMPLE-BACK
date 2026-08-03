package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaPorIdUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarPreguntaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.in.EliminarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.AResponderTipoLookupPort;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EliminarPreguntaPorIdService implements EliminarPreguntaPorIdUseCase {

    private final AResponderTipoLookupPort aResponderTipoLookupPort;
    private final EliminarPreguntaUseCase eliminarPreguntaUseCase;
    private final EliminarVerdaderoOFalsoUseCase eliminarVerdaderoOFalsoUseCase;
    private final EliminarSeleccionUnicaUseCase eliminarSeleccionUnicaUseCase;
    private final EliminarOpcionMultipleUseCase eliminarOpcionMultipleUseCase;
    private final EliminarDesplegableCompartidoUseCase eliminarDesplegableCompartidoUseCase;
    private final EliminarDesplegableIndependienteUseCase eliminarDesplegableIndependienteUseCase;

    private final Map<TipoAResponder, EliminarPreguntaGenericaUseCase> mapDeUseCases = new HashMap<>();

    public EliminarPreguntaPorIdService(AResponderTipoLookupPort aResponderTipoLookupPort,
                                         EliminarPreguntaUseCase eliminarPreguntaUseCase,
                                         EliminarVerdaderoOFalsoUseCase eliminarVerdaderoOFalsoUseCase,
                                         EliminarSeleccionUnicaUseCase eliminarSeleccionUnicaUseCase,
                                         EliminarOpcionMultipleUseCase eliminarOpcionMultipleUseCase,
                                         EliminarDesplegableCompartidoUseCase eliminarDesplegableCompartidoUseCase,
                                         EliminarDesplegableIndependienteUseCase eliminarDesplegableIndependienteUseCase) {
        this.aResponderTipoLookupPort = aResponderTipoLookupPort;
        this.eliminarPreguntaUseCase = eliminarPreguntaUseCase;
        this.eliminarVerdaderoOFalsoUseCase = eliminarVerdaderoOFalsoUseCase;
        this.eliminarSeleccionUnicaUseCase = eliminarSeleccionUnicaUseCase;
        this.eliminarOpcionMultipleUseCase = eliminarOpcionMultipleUseCase;
        this.eliminarDesplegableCompartidoUseCase = eliminarDesplegableCompartidoUseCase;
        this.eliminarDesplegableIndependienteUseCase = eliminarDesplegableIndependienteUseCase;
    }

    @PostConstruct
    private void init() {
        mapDeUseCases.put(TipoAResponder.PREGUNTA_SIMPLE, eliminarPreguntaUseCase::eliminar);
        mapDeUseCases.put(TipoAResponder.VERDADERO_FALSO, eliminarVerdaderoOFalsoUseCase::eliminar);
        mapDeUseCases.put(TipoAResponder.SELECCION_UNICA, eliminarSeleccionUnicaUseCase::eliminar);
        mapDeUseCases.put(TipoAResponder.OPCION_MULTIPLE, eliminarOpcionMultipleUseCase::eliminar);
        mapDeUseCases.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, eliminarDesplegableCompartidoUseCase::eliminar);
        mapDeUseCases.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, eliminarDesplegableIndependienteUseCase::eliminar);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        TipoAResponder tipo = aResponderTipoLookupPort.findTipoById(id)
                .orElseThrow(() -> new BussinesException("Error, no existe una pregunta con ese id"));

        EliminarPreguntaGenericaUseCase useCase = mapDeUseCases.get(tipo);
        if (useCase == null) {
            throw new BussinesException("Error, el id solicitado no corresponde a una pregunta");
        }

        useCase.eliminar(id);
    }

    @FunctionalInterface
    private interface EliminarPreguntaGenericaUseCase {
        void eliminar(Long id);
    }
}
