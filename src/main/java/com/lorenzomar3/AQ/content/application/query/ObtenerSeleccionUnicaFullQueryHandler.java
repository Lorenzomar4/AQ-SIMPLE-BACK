package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.OpcionFullDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.SeleccionUnicaFullDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerSeleccionUnicaFullQueryHandler {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public ObtenerSeleccionUnicaFullQueryHandler(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Transactional(readOnly = true)
    public SeleccionUnicaFullDTO handle(ObtenerSeleccionUnicaFullQuery query) {
        SeleccionUnica seleccionUnica = seleccionUnicaRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new SeleccionUnicaFullDTO(seleccionUnica.getId(), seleccionUnica.getTitulo(),
                seleccionUnica.getDescripcion(), seleccionUnica.getIdDuenio(), seleccionUnica.getFechaDeCreacion(),
                seleccionUnica.getUltimaActualizacion(), seleccionUnica.getTipo(),
                seleccionUnica.getIntentosParaQueDejeDeSerCriticoDisponible(), seleccionUnica.getImagenTitulo(),
                mapearOpciones(seleccionUnica));
    }

    private List<OpcionFullDTO> mapearOpciones(SeleccionUnica seleccionUnica) {
        return seleccionUnica.getListaDeOpciones().stream()
                .map(this::mapearOpcion)
                .toList();
    }

    private OpcionFullDTO mapearOpcion(Opcion opcion) {
        return new OpcionFullDTO(opcion.getId(), opcion.getOpcion(), opcion.getLaRespuestaEs());
    }
}
