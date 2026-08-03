package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoFullUseCase;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.VerdaderoOFalsoFullDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerVerdaderoOFalsoFullService implements ObtenerVerdaderoOFalsoFullUseCase {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public ObtenerVerdaderoOFalsoFullService(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public VerdaderoOFalsoFullDTO obtenerFull(Long id) {
        VerdaderoOFalso verdaderoOFalso = verdaderoOFalsoRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new VerdaderoOFalsoFullDTO(verdaderoOFalso.getId(), verdaderoOFalso.getTitulo(),
                verdaderoOFalso.getDescripcion(), verdaderoOFalso.getIdDuenio(), verdaderoOFalso.getFechaDeCreacion(),
                verdaderoOFalso.getUltimaActualizacion(), verdaderoOFalso.getTipo(),
                verdaderoOFalso.getIntentosParaQueDejeDeSerCriticoDisponible(), verdaderoOFalso.getImagenTitulo(),
                verdaderoOFalso.getRespuestaVerdadera());
    }
}
