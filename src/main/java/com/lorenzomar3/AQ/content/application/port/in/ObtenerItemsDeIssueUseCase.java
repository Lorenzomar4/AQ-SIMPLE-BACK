package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.IssueWhitItemsDTO;

public interface ObtenerItemsDeIssueUseCase {

    IssueWhitItemsDTO obtenerItems(Long id);
}
