package com.lorenzomar3.AQ.dto.newDto;

import java.time.LocalDateTime;
import java.util.List;

public record IssueWhitItemsDTO(Long id, String name, LocalDateTime creationDate, Long fatherid, List<IssueItemDTO> itemList, String type, Boolean isCritic) {}
