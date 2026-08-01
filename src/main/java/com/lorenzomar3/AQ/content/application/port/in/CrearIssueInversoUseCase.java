package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.InverseIssueCreateDTO;

public interface CrearIssueInversoUseCase {

    AResponderItemListDTO crear(InverseIssueCreateDTO inverseIssueCreateDTO);
}
