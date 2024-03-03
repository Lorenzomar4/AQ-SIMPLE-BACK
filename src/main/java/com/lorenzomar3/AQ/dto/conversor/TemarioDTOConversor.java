package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;

import java.util.Objects;

public class TemarioDTOConversor {

    public static TemarioBasicDTO toTeamarioCuestionarioCardDTO(Temario temario){
        return new TemarioBasicDTO(temario.getId(), temario.getTitulo(), temario.getFechaDeCreacion());
    }

    public static Temario fromJSON(TemarioBasicDTO temarioBasicDTO){
        Temario temario = new Temario(temarioBasicDTO.getName());

        if (!Objects.isNull(temario.getId())) {
            temario.setId(temarioBasicDTO.getId());
        }

        return temario;

    }
}
