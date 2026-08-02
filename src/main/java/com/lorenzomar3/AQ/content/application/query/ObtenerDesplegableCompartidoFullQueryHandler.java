package com.lorenzomar3.AQ.content.application.query;

import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.dto.newDto.DesplegableCompartidoFullDTO;
import com.lorenzomar3.AQ.dto.newDto.OpcionDeDesplegableCompartidoFullDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ObtenerDesplegableCompartidoFullQueryHandler {

    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public ObtenerDesplegableCompartidoFullQueryHandler(DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Transactional(readOnly = true)
    public DesplegableCompartidoFullDTO handle(ObtenerDesplegableCompartidoFullQuery query) {
        DesplegableCompartido desplegableCompartido = desplegableCompartidoRepositoryPort.findById(query.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new DesplegableCompartidoFullDTO(desplegableCompartido.getId(), desplegableCompartido.getTitulo(),
                desplegableCompartido.getDescripcion(), desplegableCompartido.getIdDuenio(), desplegableCompartido.getFechaDeCreacion(),
                desplegableCompartido.getUltimaActualizacion(), desplegableCompartido.getTipo(),
                desplegableCompartido.getIntentosParaQueDejeDeSerCriticoDisponible(), desplegableCompartido.getImagenTitulo(),
                mapearOpciones(desplegableCompartido), posiblesRespuestas(desplegableCompartido));
    }

    private List<OpcionDeDesplegableCompartidoFullDTO> mapearOpciones(DesplegableCompartido desplegableCompartido) {
        return desplegableCompartido.getListaDeOpciones().stream()
                .map(opcion -> new OpcionDeDesplegableCompartidoFullDTO(opcion.getId(), opcion.getPregunta(), opcion.getRespuesta()))
                .toList();
    }

    private List<String> posiblesRespuestas(DesplegableCompartido desplegableCompartido) {
        List<String> posibles = new ArrayList<>(desplegableCompartido.getListaDeOpciones().stream()
                .map(OpcionDeDesplegableCompartido::getRespuesta)
                .toList());
        Collections.shuffle(posibles);
        return posibles;
    }
}
