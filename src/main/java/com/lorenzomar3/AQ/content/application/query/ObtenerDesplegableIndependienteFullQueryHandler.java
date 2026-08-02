package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.dto.newDto.DesplegableIndependienteFullDTO;
import com.lorenzomar3.AQ.dto.newDto.OpcionFullDTO;
import com.lorenzomar3.AQ.dto.newDto.SeleccionUnicaParaDesplegableIndependienteFullDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerDesplegableIndependienteFullQueryHandler {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public ObtenerDesplegableIndependienteFullQueryHandler(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Transactional(readOnly = true)
    public DesplegableIndependienteFullDTO handle(ObtenerDesplegableIndependienteFullQuery query) {
        DesplegableIndependiente desplegableIndependiente = desplegableIndependienteRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new DesplegableIndependienteFullDTO(desplegableIndependiente.getId(), desplegableIndependiente.getTitulo(),
                desplegableIndependiente.getDescripcion(), desplegableIndependiente.getIdDuenio(), desplegableIndependiente.getFechaDeCreacion(),
                desplegableIndependiente.getUltimaActualizacion(), desplegableIndependiente.getTipo(),
                desplegableIndependiente.getIntentosParaQueDejeDeSerCriticoDisponible(), desplegableIndependiente.getImagenTitulo(),
                mapearSubPreguntas(desplegableIndependiente));
    }

    private List<SeleccionUnicaParaDesplegableIndependienteFullDTO> mapearSubPreguntas(DesplegableIndependiente desplegableIndependiente) {
        return desplegableIndependiente.getListaDeOpciones().stream()
                .map(this::mapearSubPregunta)
                .toList();
    }

    private SeleccionUnicaParaDesplegableIndependienteFullDTO mapearSubPregunta(SeleccionUnicaParaDesplegableIndependiente subPregunta) {
        return new SeleccionUnicaParaDesplegableIndependienteFullDTO(subPregunta.getId(), subPregunta.getTitulo(),
                subPregunta.getListaDeOpciones().stream().map(this::mapearOpcion).toList());
    }

    private OpcionFullDTO mapearOpcion(Opcion opcion) {
        return new OpcionFullDTO(opcion.getId(), opcion.getOpcion(), opcion.getLaRespuestaEs());
    }
}
