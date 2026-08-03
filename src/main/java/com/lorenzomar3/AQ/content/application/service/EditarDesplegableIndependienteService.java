package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableIndependienteUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableIndependienteRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.dto.newDto.PostPreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarDesplegableIndependienteService implements EditarDesplegableIndependienteUseCase {

    private final DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort;

    public EditarDesplegableIndependienteService(DesplegableIndependienteRepositoryPort desplegableIndependienteRepositoryPort) {
        this.desplegableIndependienteRepositoryPort = desplegableIndependienteRepositoryPort;
    }

    @Override
    @Transactional
    public DesplegableIndependiente editar(PostPreguntaDTO postPreguntaDTO) {
        DesplegableIndependiente desplegableIndependiente = desplegableIndependienteRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        desplegableIndependiente.setTitulo(postPreguntaDTO.titulo());
        desplegableIndependiente.setDescripcion(postPreguntaDTO.descripcion());
        desplegableIndependiente.setListaDeOpciones(postPreguntaDTO.listaDeOpcionDesplegableIndependiente());

        return desplegableIndependienteRepositoryPort.save(desplegableIndependiente);
    }
}
