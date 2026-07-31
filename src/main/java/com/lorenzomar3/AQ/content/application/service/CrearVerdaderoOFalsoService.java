package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.VerdaderoOFalsoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearVerdaderoOFalsoService implements CrearVerdaderoOFalsoUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort;

    public CrearVerdaderoOFalsoService(TemarioRepositoryPort temarioRepositoryPort, VerdaderoOFalsoRepositoryPort verdaderoOFalsoRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.verdaderoOFalsoRepositoryPort = verdaderoOFalsoRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        VerdaderoOFalso verdaderoOFalso = new VerdaderoOFalso();
        verdaderoOFalso.setTitulo(postPreguntaDTO.titulo());
        verdaderoOFalso.setDescripcion(postPreguntaDTO.descripcion());
        verdaderoOFalso.setRespuestaVerdadera(postPreguntaDTO.respuestaVerdadera());
        verdaderoOFalso.setTipo(TipoAResponder.VERDADERO_FALSO);
        verdaderoOFalso.setIdDuenio(temario.getId());
        verdaderoOFalso.setFechaDeCreacion(LocalDateTime.now());
        verdaderoOFalso.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        verdaderoOFalsoRepositoryPort.save(verdaderoOFalso);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }
}
