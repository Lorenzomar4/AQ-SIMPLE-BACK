package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.IssueWhitItemsDTO;

public interface ObtenerItemsDeIssueUseCase {

    IssueWhitItemsDTO obtenerItems(Long id);
}
