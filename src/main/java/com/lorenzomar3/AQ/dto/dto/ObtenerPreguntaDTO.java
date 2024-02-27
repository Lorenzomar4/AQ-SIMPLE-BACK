package com.lorenzomar3.AQ.dto.dto;

import com.lorenzomar3.AQ.model.TipoAResponder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ObtenerPreguntaDTO {
    Long id;
    TipoAResponder tipoAResponder;

}
