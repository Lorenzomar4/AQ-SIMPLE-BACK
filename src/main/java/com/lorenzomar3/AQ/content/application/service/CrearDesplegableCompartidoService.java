package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
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
public class CrearDesplegableCompartidoService implements CrearDesplegableCompartidoUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;
    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public CrearDesplegableCompartidoService(TemarioRepositoryPort temarioRepositoryPort, DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Override
    @Transactional
    public CreateQuestionResponseDTO crear(PostPreguntaDTO postPreguntaDTO) {
        Temario temario = temarioRepositoryPort.findById(postPreguntaDTO.idTemarioPerteneciente())
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        DesplegableCompartido desplegableCompartido = new DesplegableCompartido();
        desplegableCompartido.setTitulo(postPreguntaDTO.titulo());
        desplegableCompartido.setDescripcion(postPreguntaDTO.descripcion());
        desplegableCompartido.setListaDeOpciones(convertirOpciones(postPreguntaDTO));
        desplegableCompartido.setTipo(TipoAResponder.DESPLEGABLE_COMPARTIDO);
        desplegableCompartido.setIdDuenio(temario.getId());
        desplegableCompartido.setFechaDeCreacion(LocalDateTime.now());
        desplegableCompartido.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        desplegableCompartidoRepositoryPort.save(desplegableCompartido);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }

    private List<OpcionDeDesplegableCompartido> convertirOpciones(PostPreguntaDTO postPreguntaDTO) {
        return postPreguntaDTO.listaDeOpcionDesplegableCompartido().stream().map(opcionVieja -> {
            OpcionDeDesplegableCompartido opcion = new OpcionDeDesplegableCompartido();
            opcion.setPregunta(opcionVieja.getPregunta());
            opcion.setRespuesta(opcionVieja.getRespuestaCorrecta());
            return opcion;
        }).toList();
    }
}
