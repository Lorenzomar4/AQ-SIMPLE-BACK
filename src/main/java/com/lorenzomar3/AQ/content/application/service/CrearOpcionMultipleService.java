package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearOpcionMultipleService implements CrearOpcionMultipleUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final OpcionMultipleRepositoryPort opcionMultipleRepositoryPort;

    public CrearOpcionMultipleService(TemarioRepositoryPort temarioRepositoryPort, OpcionMultipleRepositoryPort opcionMultipleRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.opcionMultipleRepositoryPort = opcionMultipleRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        OpcionMultiple opcionMultiple = new OpcionMultiple();
        opcionMultiple.setTitulo(postPreguntaDTO.titulo());
        opcionMultiple.setDescripcion(postPreguntaDTO.descripcion());
        opcionMultiple.setListaDeOpciones(postPreguntaDTO.listaDeOpcionesConSuRespuestaReal());
        opcionMultiple.setTipo(TipoAResponder.OPCION_MULTIPLE);
        opcionMultiple.setIdDuenio(temario.getId());
        opcionMultiple.setFechaDeCreacion(LocalDateTime.now());
        opcionMultiple.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        opcionMultipleRepositoryPort.save(opcionMultiple);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }
}
