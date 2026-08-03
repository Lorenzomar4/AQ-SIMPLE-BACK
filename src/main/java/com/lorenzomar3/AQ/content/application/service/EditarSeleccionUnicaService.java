package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.EditarSeleccionUnicaUseCase;
import com.lorenzomar3.AQ.content.application.port.out.SeleccionUnicaRepositoryPort;
import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.PostPreguntaDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EditarSeleccionUnicaService implements EditarSeleccionUnicaUseCase {

    private final SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort;

    public EditarSeleccionUnicaService(SeleccionUnicaRepositoryPort seleccionUnicaRepositoryPort) {
        this.seleccionUnicaRepositoryPort = seleccionUnicaRepositoryPort;
    }

    @Override
    @Transactional
    public SeleccionUnica editar(PostPreguntaDTO postPreguntaDTO) {
        SeleccionUnica seleccionUnica = seleccionUnicaRepositoryPort.findById(postPreguntaDTO.id())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el tipo De id solicitadO"));

        seleccionUnica.setTitulo(postPreguntaDTO.titulo());
        seleccionUnica.setDescripcion(postPreguntaDTO.descripcion());
        seleccionUnica.setListaDeOpciones(postPreguntaDTO.listaDeOpcionesConSuRespuestaReal());

        return seleccionUnicaRepositoryPort.save(seleccionUnica);
    }
}
