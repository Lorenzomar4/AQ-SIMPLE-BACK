package com.lorenzomar3.AQ.dto.newDto;

import java.time.LocalDateTime;
import java.util.List;

public record CuestionarioWithListDTO(Long id, String name, LocalDateTime creationDate, List<AResponderItemListDTO> itemList) {}