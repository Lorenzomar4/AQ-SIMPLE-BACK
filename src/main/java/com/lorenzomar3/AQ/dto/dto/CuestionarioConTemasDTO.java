package com.lorenzomar3.AQ.dto.dto;

import java.util.List;

public class CuestionarioConTemasDTO {

    public CuestionarioSimpleDTO cuestionario;

    public List<TemaSinPreguntasDTO> temas;


    public CuestionarioConTemasDTO(CuestionarioSimpleDTO cuestionario, List<TemaSinPreguntasDTO> temas) {
        this.cuestionario = cuestionario;
        this.temas = temas;
    }
}
