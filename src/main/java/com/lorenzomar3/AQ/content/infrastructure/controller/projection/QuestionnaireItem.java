package com.lorenzomar3.AQ.content.infrastructure.controller.projection;

import com.lorenzomar3.AQ.content.api.TipoAResponder;

import java.time.LocalDateTime;

public interface QuestionnaireItem {
    Long getId();

    String getName();

    TipoAResponder getType();

    LocalDateTime getCreationDate();

    Boolean getIsCritic();

    Integer getNumberOfQuestions();

}
