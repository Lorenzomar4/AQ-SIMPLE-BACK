package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.dto.newDto.VerdaderoOFalsoFetchDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObtenerVerdaderoOFalsoService implements ObtenerVerdaderoOFalsoUseCase {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public ObtenerVerdaderoOFalsoService(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public VerdaderoOFalsoFetchDTO obtener(Long id) {
        VerdaderoOFalso verdaderoOFalso = verdaderoOFalsoRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        return new VerdaderoOFalsoFetchDTO(verdaderoOFalso.getId(), verdaderoOFalso.getTitulo(),
                verdaderoOFalso.getDescripcion(), verdaderoOFalso.getIdDuenio(), verdaderoOFalso.getFechaDeCreacion(),
                verdaderoOFalso.getUltimaActualizacion(), verdaderoOFalso.getTipo(),
                verdaderoOFalso.getIntentosParaQueDejeDeSerCriticoDisponible(), verdaderoOFalso.getImagenTitulo());
    }
}
