package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.CrearDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
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
        desplegableIndependiente.setListaDeOpciones(convertirListaDeOpciones(postPreguntaDTO));
        desplegableIndependiente.setTipo(TipoAResponder.DESPLEGABLE_INDEPENDIENTE);
        desplegableIndependiente.setIdDuenio(temario.getId());
        desplegableIndependiente.setFechaDeCreacion(LocalDateTime.now());
        desplegableIndependiente.setIntentosParaQueDejeDeSerCriticoDisponible(0);

        desplegableIndependienteRepositoryPort.save(desplegableIndependiente);

        return new CreateQuestionResponseDTO(temario.getId(), temario.getTipo());
    }

    private List<SeleccionUnicaParaDesplegableIndependiente> convertirListaDeOpciones(PostPreguntaDTO postPreguntaDTO) {
        return postPreguntaDTO.listaDeOpcionDesplegableIndependiente().stream().map(subPreguntaVieja -> {
            SeleccionUnicaParaDesplegableIndependiente subPregunta = new SeleccionUnicaParaDesplegableIndependiente();
            subPregunta.setTitulo(subPreguntaVieja.getTitulo());
            subPregunta.setListaDeOpciones(
                    subPreguntaVieja.getListaDeOpcionesDisponible().stream().map(opcionVieja -> {
                        Opcion opcion = new Opcion();
                        opcion.setOpcion(opcionVieja.getOpcion());
                        opcion.setLaRespuestaEs(opcionVieja.getRespuestaCorrecta());
                        return opcion;
                    }).toList()
            );
            return subPregunta;
        }).toList();
    }
}
