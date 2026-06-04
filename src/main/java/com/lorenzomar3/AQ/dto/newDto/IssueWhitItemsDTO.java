package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.projections.QuestionnaireItem;

import java.time.LocalDateTime;
import java.util.List;

public record IssueWhitItemsDTO(Long id, String name, LocalDateTime creationDate, Long fatherid, List<QuestionnaireItem> itemList, String type, Boolean isCritic) {}
