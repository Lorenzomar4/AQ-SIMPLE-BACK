package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsAleatoriosDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.PreguntaSimpleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.ObtenerPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class ObtenerIdsAleatoriosDePreguntasService implements ObtenerIdsAleatoriosDePreguntasUseCase {

    private static final List<TipoAResponder> TIPOS_CONTENEDOR =
            List.of(TipoAResponder.CUESTIONARIO, TipoAResponder.TEMA, TipoAResponder.SUBTEMA);

    private final ObtenerIdsDePreguntasUseCase obtenerIdsDePreguntasUseCase;
    private final PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort;
    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;
    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;
    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;
    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;
    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    private final Map<TipoAResponder, Function<Long, Boolean>> mapDeExistencia = new HashMap<>();

    public ObtenerIdsAleatoriosDePreguntasService(ObtenerIdsDePreguntasUseCase obtenerIdsDePreguntasUseCase,
                                                    PreguntaSimpleRepositoryPort preguntaSimpleRepositoryPort,
                                                    VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort,
                                                    SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort,
                                                    OpcionMultipleRepositoryPort opcionMultipleRepositoryPort,
                                                    DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort,
                                                    DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.obtenerIdsDePreguntasUseCase = obtenerIdsDePreguntasUseCase;
        this.preguntaSimpleRepositoryPort = preguntaSimpleRepositoryPort;
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @PostConstruct
    private void init() {
        mapDeExistencia.put(TipoAResponder.PREGUNTA_SIMPLE, id -> preguntaSimpleRepositoryPort.findById(id).isPresent());
        mapDeExistencia.put(TipoAResponder.VERDADERO_FALSO, id -> verdaderoOFalsoRepositoryPort.findById(id).isPresent());
        mapDeExistencia.put(TipoAResponder.SELECCION_UNICA, id -> seleccionUnicaRepositoryPort.findById(id).isPresent());
        mapDeExistencia.put(TipoAResponder.OPCION_MULTIPLE, id -> opcionMultipleRepositoryPort.findById(id).isPresent());
        mapDeExistencia.put(TipoAResponder.DESPLEGABLE_COMPARTIDO, id -> desplegableCompartidoRepositoryPort.findById(id).isPresent());
        mapDeExistencia.put(TipoAResponder.DESPLEGABLE_INDEPENDIENTE, id -> desplegableIndependienteRepositoryPort.findById(id).isPresent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerIds(ObtenerPreguntaDTO dto) {
        List<Long> ids;
        if (TIPOS_CONTENEDOR.contains(dto.tipoAResponder())) {
            ids = obtenerIdsDePreguntasUseCase.obtenerIdsDePreguntas(dto.id());
        } else {
            Function<Long, Boolean> existePorId = mapDeExistencia.get(dto.tipoAResponder());
            if (existePorId == null) {
                throw new BussinesException("Error, el tipo solicitado no está soportado");
            }
            if (!existePorId.apply(dto.id())) {
                throw new BussinesException("Error no existe una pregunta con ese id");
            }
            ids = List.of(dto.id());
        }

        List<Long> idsMezclados = new ArrayList<>(ids);
        Collections.shuffle(idsMezclados);
        return idsMezclados;
    }
}
