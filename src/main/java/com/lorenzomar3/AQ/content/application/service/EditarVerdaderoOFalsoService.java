package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarVerdaderoOFalsoService implements EditarVerdaderoOFalsoUseCase {

    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public EditarVerdaderoOFalsoService(VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    @Transactional
    public VerdaderoOFalso editar(PostPreguntaDTO postPreguntaDTO) {
        VerdaderoOFalso verdaderoOFalso = verdaderoOFalsoRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        verdaderoOFalso.setTitulo(postPreguntaDTO.titulo());
        verdaderoOFalso.setDescripcion(postPreguntaDTO.descripcion());
        verdaderoOFalso.setRespuestaVerdadera(postPreguntaDTO.respuestaVerdadera());

        return verdaderoOFalsoRepositoryPort.save(verdaderoOFalso);
    }
}
