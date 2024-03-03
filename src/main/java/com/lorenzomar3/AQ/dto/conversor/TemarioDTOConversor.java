package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioCardDTO;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.Cuestionario;

import java.util.Objects;

public class TemarioDTOConversor {

    public static TemarioCuestionarioCardDTO toTeamarioCuestionarioCardDTO(Temario temario){
        return new TemarioCuestionarioCardDTO(temario.getId(), temario.getTitulo(), temario.getFechaDeCreacion());
    }

    public static Temario fromJSON(TemarioCuestionarioCardDTO temarioCuestionarioCardDTO){
        Temario temario = new Temario(temarioCuestionarioCardDTO.getName());

        if (!Objects.isNull(temario.getId())) {
            temario.setId(temarioCuestionarioCardDTO.getId());
        }

        return temario;

    }
}
