package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.OpcionFetchDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.OpcionMultipleFetchDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerOpcionMultipleQueryHandler {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public ObtenerOpcionMultipleQueryHandler(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Transactional(readOnly = true)
    public OpcionMultipleFetchDTO handle(ObtenerOpcionMultipleQuery query) {
        OpcionMultiple opcionMultiple = opcionMultipleRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new OpcionMultipleFetchDTO(opcionMultiple.getId(), opcionMultiple.getTitulo(),
                opcionMultiple.getDescripcion(), opcionMultiple.getIdDuenio(), opcionMultiple.getFechaDeCreacion(),
                opcionMultiple.getUltimaActualizacion(), opcionMultiple.getTipo(),
                opcionMultiple.getIntentosParaQueDejeDeSerCriticoDisponible(), opcionMultiple.getImagenTitulo(),
                mapearOpciones(opcionMultiple));
    }

    private List<OpcionFetchDTO> mapearOpciones(OpcionMultiple opcionMultiple) {
        return opcionMultiple.getListaDeOpciones().stream()
                .map(this::mapearOpcion)
                .toList();
    }

    private OpcionFetchDTO mapearOpcion(Opcion opcion) {
        return new OpcionFetchDTO(opcion.getId(), opcion.getOpcion());
    }
}
