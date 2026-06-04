package com.lorenzomar3.AQ.dto.newDto;

import java.time.LocalDateTime;

public record TemarioBasicDTO(Long id, String name, LocalDateTime creationDate, Long fatherid) {}