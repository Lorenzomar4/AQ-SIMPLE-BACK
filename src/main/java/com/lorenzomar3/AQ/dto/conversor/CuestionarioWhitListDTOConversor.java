package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioWithListDTO;
import com.lorenzomar3.AQ.model.Cuestionario;

import java.util.List;

public class CuestionarioWhitListDTOConversor {

    public static CuestionarioWithListDTO toDTO(Cuestionario cuestionario){
        List<AResponderItemListDTO> itemListDTO =
                cuestionario.getListaAResponder().stream().map(item -> item.toResponderItemListDTO()).toList();
        
        return new CuestionarioWithListDTO(cuestionario.getId(),cuestionario.getNombreCuestionario(),
                cuestionario.getFechaDeCreacion() ,itemListDTO);

    }
}
