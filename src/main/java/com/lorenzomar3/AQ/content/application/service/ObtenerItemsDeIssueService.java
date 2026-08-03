package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerItemsDeIssueUseCase;
import com.lorenzomar3.AQ.content.application.port.out.AResponderItemDetail;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.IssueItemDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.IssueWhitItemsDTO;
import com.lorenzomar3.AQ.shared.exception.BussinesException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObtenerItemsDeIssueService implements ObtenerItemsDeIssueUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public ObtenerItemsDeIssueService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public IssueWhitItemsDTO obtenerItems(Long id) {
        Temario raiz = temarioRepositoryPort.findById(id)
                .orElseThrow(() -> new BussinesException("No existe ese cuestionario"));

        List<AResponderItemDetail> items = temarioRepositoryPort.findIssueItems(id);

        AResponderItemDetail filaRaiz = items.stream()
                .filter(item -> item.id().equals(id))
                .findFirst().get();

        List<IssueItemDTO> itemList = items.stream()
                .filter(item -> !item.id().equals(id))
                .map(item -> new IssueItemDTO(item.id(), item.titulo(), item.tipo(), item.fechaDeCreacion(),
                        item.esCritico(), item.numeroDePreguntas()))
                .toList();

        return new IssueWhitItemsDTO(raiz.getId(), raiz.getTitulo(), raiz.getFechaDeCreacion(), raiz.getIdDuenio(),
                itemList, raiz.getTipo().name(), filaRaiz.esCritico());
    }
}
