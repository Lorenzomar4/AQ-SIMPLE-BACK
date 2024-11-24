package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
public class IssueWhitItemsDTO {
    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private Long fatherid;
    private List<QuestionnaireItem> itemList;

    public IssueWhitItemsDTO(Long id, String name, LocalDateTime creationDate, Long fatherid , List<QuestionnaireItem> itemList) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.fatherid = fatherid;
        this.itemList = itemList;
    }
}
