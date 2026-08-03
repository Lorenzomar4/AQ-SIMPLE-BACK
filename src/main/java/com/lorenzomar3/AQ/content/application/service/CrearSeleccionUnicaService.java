package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        seleccionUnica.setListaDeOpciones(postPreguntaDTO.listaDeOpcionesConSuRespuestaReal());
        seleccionUnica.setTipo(TipoAResponder.SELECCION_UNICA);
        seleccionUnica.setIdDuenio(temario.getId());
        seleccionUnica.setFechaDeCreacion(LocalDateTime.now());
        seleccionUnica.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        seleccionUnicaRepositoryPort.save(seleccionUnica);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }
}
