package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import java.time.LocalDateTime;

public record CuestionarioDTO(Long id, String name, LocalDateTime creationDate) {}