package com.lorenzomar3.AQ.projections;

import com.lorenzomar3.AQ.model.TipoAResponder;

import java.time.LocalDateTime;

public interface QuestionnaireItem {
    Long getId();

    String getName();

    TipoAResponder getType();

    LocalDateTime getCreationDate();

    Boolean getIsCritic();

    Integer getNumberOfQuestions();

}
