package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class IssueWhitItemsDTO {
    private Long id;
    private String name;
    private LocalDateTime creationDate;
    private Long fatherid;
    private List<QuestionnaireItem> itemList;
    private String type;
    private Boolean isCritic;

    public IssueWhitItemsDTO(Long id, String name, LocalDateTime creationDate, Long fatherid , List<QuestionnaireItem> itemList , String type , Boolean isCritic ) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.fatherid = fatherid;
        this.itemList = itemList;
        this.type = type;
        this.isCritic = isCritic;
    }
}
