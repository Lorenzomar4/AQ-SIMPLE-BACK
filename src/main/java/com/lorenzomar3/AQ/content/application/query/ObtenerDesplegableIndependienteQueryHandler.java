package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.dto.newDto.DesplegableIndependienteFetchDTO;
import com.lorenzomar3.AQ.dto.newDto.OpcionFetchDTO;
import com.lorenzomar3.AQ.dto.newDto.SeleccionUnicaParaDesplegableIndependienteFetchDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerDesplegableIndependienteQueryHandler {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public ObtenerDesplegableIndependienteQueryHandler(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Transactional(readOnly = true)
    public DesplegableIndependienteFetchDTO handle(ObtenerDesplegableIndependienteQuery query) {
        DesplegableIndependiente desplegableIndependiente = desplegableIndependienteRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new DesplegableIndependienteFetchDTO(desplegableIndependiente.getId(), desplegableIndependiente.getTitulo(),
                desplegableIndependiente.getDescripcion(), desplegableIndependiente.getIdDuenio(), desplegableIndependiente.getFechaDeCreacion(),
                desplegableIndependiente.getUltimaActualizacion(), desplegableIndependiente.getTipo(),
                desplegableIndependiente.getIntentosParaQueDejeDeSerCriticoDisponible(), desplegableIndependiente.getImagenTitulo(),
                mapearSubPreguntas(desplegableIndependiente));
    }

    private List<SeleccionUnicaParaDesplegableIndependienteFetchDTO> mapearSubPreguntas(DesplegableIndependiente desplegableIndependiente) {
        return desplegableIndependiente.getListaDeOpciones().stream()
                .map(this::mapearSubPregunta)
                .toList();
    }

    private SeleccionUnicaParaDesplegableIndependienteFetchDTO mapearSubPregunta(SeleccionUnicaParaDesplegableIndependiente subPregunta) {
        return new SeleccionUnicaParaDesplegableIndependienteFetchDTO(subPregunta.getId(), subPregunta.getTitulo(),
                subPregunta.getListaDeOpciones().stream().map(this::mapearOpcion).toList());
    }

    private OpcionFetchDTO mapearOpcion(Opcion opcion) {
        return new OpcionFetchDTO(opcion.getId(), opcion.getOpcion());
    }
}
