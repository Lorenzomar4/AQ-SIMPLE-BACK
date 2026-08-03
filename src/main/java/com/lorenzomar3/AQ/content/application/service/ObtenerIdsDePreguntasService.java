package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsDePreguntasUseCase;
import com.lorenzomar3.AQ.content.application.port.out.AResponderChildRef;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObtenerIdsDePreguntasService implements ObtenerIdsDePreguntasUseCase {

    private static final List<TipoAResponder> TIPOS_CONTENEDOR =
            List.of(TipoAResponder.CUESTIONARIO, TipoAResponder.TEMA, TipoAResponder.SUBTEMA);

    private final TemarioRepositoryPort temarioRepositoryPort;

    public ObtenerIdsDePreguntasService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerIdsDePreguntas(Long id) {
        temarioRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("Error no existe un temario con ese id"));

        List<Long> ids = new ArrayList<>();
        recolectarIds(id, ids);
        return ids;
    }

    private void recolectarIds(Long id, List<Long> ids) {
        for (AResponderChildRef hijo : temarioRepositoryPort.findDirectChildren(id)) {
            if (TIPOS_CONTENEDOR.contains(hijo.tipo())) {
                recolectarIds(hijo.id(), ids);
            } else {
                ids.add(hijo.id());
            }
        }
    }
}
