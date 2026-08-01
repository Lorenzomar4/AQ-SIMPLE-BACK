package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;

public record IssueItemDTO(Long id, String name, TipoAResponder type, LocalDateTime creationDate, Boolean isCritic, Integer numberOfQuestions) {}
