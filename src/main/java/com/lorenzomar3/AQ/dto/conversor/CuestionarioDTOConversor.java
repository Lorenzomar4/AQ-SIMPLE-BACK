package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.model.Cuestionario;

import java.util.Objects;

public class CuestionarioDTOConversor {

    public static CuestionarioDTO toDTO(Cuestionario cuestionario) {
        return new CuestionarioDTO(cuestionario.getId(), cuestionario.getTitulo(), cuestionario.getFechaDeCreacion());
    }

    public static  Cuestionario fromJSON(CuestionarioDTO cuestionarioDTO) {

        Cuestionario cuestionario = new Cuestionario(cuestionarioDTO.getName());

        if (!Objects.isNull(cuestionarioDTO.getId())) {
            cuestionario.setId(cuestionarioDTO.getId());
        }

        return cuestionario;

    }


}
