package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
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
public class CrearSeleccionUnicaService implements CrearSeleccionUnicaUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public CrearSeleccionUnicaService(TemarioRepositoryPort temarioRepositoryPort, SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        SeleccionUnica seleccionUnica = new SeleccionUnica();
        seleccionUnica.setTitulo(postPreguntaDTO.titulo());
        seleccionUnica.setDescripcion(postPreguntaDTO.descripcion());
        seleccionUnica.setListaDeOpciones(convertirOpciones(postPreguntaDTO));
        seleccionUnica.setTipo(TipoAResponder.SELECCION_UNICA);
        seleccionUnica.setIdDuenio(temario.getId());
        seleccionUnica.setFechaDeCreacion(LocalDateTime.now());
        seleccionUnica.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        seleccionUnicaRepositoryPort.save(seleccionUnica);

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
