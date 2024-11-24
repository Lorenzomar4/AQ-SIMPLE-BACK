package com.lorenzomar3.AQ.projections;

import java.time.LocalDateTime;

public interface QuestionnaireItem {
    Long getId();

    String getName();

    LocalDateTime getCreationDate();

}
