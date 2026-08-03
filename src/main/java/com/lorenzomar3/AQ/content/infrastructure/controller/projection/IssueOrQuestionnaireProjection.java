package com.lorenzomar3.AQ.content.infrastructure.controller.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IssueOrQuestionnaireProjection {
    Long getId();

    String getName();

    LocalDateTime getCreationDate();

    Long getFatherId();

    String getType();

}
