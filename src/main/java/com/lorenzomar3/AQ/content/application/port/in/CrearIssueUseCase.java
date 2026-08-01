package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;

public interface CrearIssueUseCase {

    AResponderItemListDTO crear(TemarioBasicDTO temarioBasicDTO);
}
