package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearOpcionMultipleUseCase;
import com.lorenzomar3.AQ.content.application.port.out.OpcionMultipleRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.dto.newDto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        opcionMultiple.setListaDeOpciones(convertirOpciones(postPreguntaDTO));
        opcionMultiple.setTipo(TipoAResponder.OPCION_MULTIPLE);
        opcionMultiple.setIdDuenio(temario.getId());
        opcionMultiple.setFechaDeCreacion(LocalDateTime.now());
        opcionMultiple.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        opcionMultipleRepositoryPort.save(opcionMultiple);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }

    private List<Opcion> convertirOpciones(PostPreguntaDTO postPreguntaDTO) {
        return postPreguntaDTO.listaDeOpcionesConSuRespuestaReal().stream().map(opcionVieja -> {
            Opcion opcion = new Opcion();
            opcion.setOpcion(opcionVieja.getOpcion());
            opcion.setLaRespuestaEs(opcionVieja.getRespuestaCorrecta());
            return opcion;
        }).toList();
    }
}
