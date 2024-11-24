package com.lorenzomar3.AQ.projections;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IssueOrQuestionnaireProjection {
    Long getId();

    String getName();

    LocalDateTime getCreationDate();

    Long getFatherId();

}
