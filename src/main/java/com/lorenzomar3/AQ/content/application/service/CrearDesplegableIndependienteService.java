package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.CreateQuestionResponseDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import com.lorenzomar3.AQ.content.api.TipoAResponder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearDesplegableIndependienteService implements CrearDesplegableIndependienteUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public CrearDesplegableIndependienteService(TemarioRepositoryPort temarioRepositoryPort, DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        DesplegableIndependiente desplegableIndependiente = new DesplegableIndependiente();
        desplegableIndependiente.setTitulo(postPreguntaDTO.titulo());
        desplegableIndependiente.setDescripcion(postPreguntaDTO.descripcion());
        desplegableIndependiente.setListaDeOpciones(postPreguntaDTO.listaDeOpcionDesplegableIndependiente());
        desplegableIndependiente.setTipo(TipoAResponder.DESPLEGABLE_INDEPENDIENTE);
        desplegableIndependiente.setIdDuenio(temario.getId());
        desplegableIndependiente.setFechaDeCreacion(LocalDateTime.now());
        desplegableIndependiente.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        desplegableIndependienteRepositoryPort.save(desplegableIndependiente);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }
}
