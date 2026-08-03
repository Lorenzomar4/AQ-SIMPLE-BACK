package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarDesplegableCompartidoUseCase;
import com.lorenzomar3.AQ.content.application.port.out.DesplegableCompartidoRepositoryPort;
import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarDesplegableCompartidoService implements EditarDesplegableCompartidoUseCase {

    private final DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort;

    public EditarDesplegableCompartidoService(DesplegableCompartidoRepositoryPort desplegableCompartidoRepositoryPort) {
        this.desplegableCompartidoRepositoryPort = desplegableCompartidoRepositoryPort;
    }

    @Override
    @Transactional
    public DesplegableCompartido editar(PostPreguntaDTO postPreguntaDTO) {
        DesplegableCompartido desplegableCompartido = desplegableCompartidoRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        desplegableCompartido.setTitulo(postPreguntaDTO.titulo());
        desplegableCompartido.setDescripcion(postPreguntaDTO.descripcion());
        desplegableCompartido.setListaDeOpciones(postPreguntaDTO.listaDeOpcionDesplegableCompartido());

        return desplegableCompartidoRepositoryPort.save(desplegableCompartido);
    }
}
