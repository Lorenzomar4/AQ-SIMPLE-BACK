package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.dto.newDto.OpcionFetchDTO;
import com.lorenzomar3.AQ.dto.newDto.SeleccionUnicaFetchDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerSeleccionUnicaQueryHandler {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public ObtenerSeleccionUnicaQueryHandler(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Transactional(readOnly = true)
    public SeleccionUnicaFetchDTO handle(ObtenerSeleccionUnicaQuery query) {
        SeleccionUnica seleccionUnica = seleccionUnicaRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new SeleccionUnicaFetchDTO(seleccionUnica.getId(), seleccionUnica.getTitulo(),
                seleccionUnica.getDescripcion(), seleccionUnica.getIdDuenio(), seleccionUnica.getFechaDeCreacion(),
                seleccionUnica.getUltimaActualizacion(), seleccionUnica.getTipo(),
                seleccionUnica.getIntentosParaQueDejeDeSerCriticoDisponible(), seleccionUnica.getImagenTitulo(),
                mapearOpciones(seleccionUnica));
    }

    private List<OpcionFetchDTO> mapearOpciones(SeleccionUnica seleccionUnica) {
        return seleccionUnica.getListaDeOpciones().stream()
                .map(this::mapearOpcion)
                .toList();
    }

    private OpcionFetchDTO mapearOpcion(Opcion opcion) {
        return new OpcionFetchDTO(opcion.getId(), opcion.getOpcion());
    }
}
