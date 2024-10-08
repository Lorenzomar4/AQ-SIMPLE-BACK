package com.lorenzomar3.AQ.dto.newDto;

import com.lorenzomar3.AQ.model.TipoAResponder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AResponderItemListDTO {

    public Long id;

    public String name;
    public TipoAResponder type;
    public Integer numberOfQuestions;
    public Boolean isCritic;

    public AResponderItemListDTO(Long id, String name, TipoAResponder type, Integer numberOfQuestions, Boolean isCritic) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.numberOfQuestions = numberOfQuestions;
        this.isCritic = isCritic;
    }
}
