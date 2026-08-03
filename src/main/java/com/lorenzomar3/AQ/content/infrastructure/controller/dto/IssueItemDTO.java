package com.lorenzomar3.AQ.content.infrastructure.controller.dto;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.time.LocalDateTime;

public record IssueItemDTO(Long id, String name, TipoAResponder type, LocalDateTime creationDate, Boolean isCritic, Integer numberOfQuestions) {}
