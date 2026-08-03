package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.AResponderItemListDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.InverseIssueCreateDTO;

public interface CrearIssueInversoUseCase {

    AResponderItemListDTO crear(InverseIssueCreateDTO inverseIssueCreateDTO);
}
