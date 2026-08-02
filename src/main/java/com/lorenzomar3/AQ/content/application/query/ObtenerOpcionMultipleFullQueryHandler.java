package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.dto.newDto.OpcionFullDTO;
import com.lorenzomar3.AQ.dto.newDto.OpcionMultipleFullDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerOpcionMultipleFullQueryHandler {

    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public ObtenerOpcionMultipleFullQueryHandler(OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Transactional(readOnly = true)
    public OpcionMultipleFullDTO handle(ObtenerOpcionMultipleFullQuery query) {
        OpcionMultiple opcionMultiple = opcionMultipleRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new OpcionMultipleFullDTO(opcionMultiple.getId(), opcionMultiple.getTitulo(),
                opcionMultiple.getDescripcion(), opcionMultiple.getIdDuenio(), opcionMultiple.getFechaDeCreacion(),
                opcionMultiple.getUltimaActualizacion(), opcionMultiple.getTipo(),
                opcionMultiple.getIntentosParaQueDejeDeSerCriticoDisponible(), opcionMultiple.getImagenTitulo(),
                mapearOpciones(opcionMultiple));
    }

    private List<OpcionFullDTO> mapearOpciones(OpcionMultiple opcionMultiple) {
        return opcionMultiple.getListaDeOpciones().stream()
                .map(this::mapearOpcion)
                .toList();
    }

    private OpcionFullDTO mapearOpcion(Opcion opcion) {
        return new OpcionFullDTO(opcion.getId(), opcion.getOpcion(), opcion.getLaRespuestaEs());
    }
}
