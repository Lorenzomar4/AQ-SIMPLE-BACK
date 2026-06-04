package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;

import java.util.Objects;

public class TemarioDTOConversor {

    public static TemarioBasicDTO toTeamarioCuestionarioCardDTO(Temario temario){
        return new TemarioBasicDTO(temario.getId(), temario.getTitulo(), temario.getFechaDeCreacion(), null);
    }

    public static Temario fromJSON(TemarioBasicDTO temarioBasicDTO){
        Temario temario = new Temario(temarioBasicDTO.name());

        if (!Objects.isNull(temario.getId())) {
            temario.setId(temarioBasicDTO.id());
        }

        return temario;

    }
}
