package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.infrastructure.controller.dto.AResponderItemListDTO;
import com.lorenzomar3.AQ.content.infrastructure.controller.dto.TemarioBasicDTO;

public interface CrearIssueUseCase {

    AResponderItemListDTO crear(TemarioBasicDTO temarioBasicDTO);
}
