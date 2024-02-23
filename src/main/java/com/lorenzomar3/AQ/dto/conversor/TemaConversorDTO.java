package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Tema;

import java.util.List;

public class TemaConversorDTO {

    public static Tema fromJSON(TemaPostDTO temaPostDTO){
        Tema tema = new Tema();

        tema.setId(temaPostDTO.getId());
        tema.setTitulo(temaPostDTO.getName());
        return tema;
    }

    public static TemaDTO fullTemaDTO(Tema tema){
        final List<AResponderItemListDTO> itemList = tema.contenidoAResponder()
                .stream().map(AResponder::toResponderItemListDTO)
                .toList();


        return new TemaDTO(tema.getId(),
                tema.getTitulo(),
                tema.getFechaDeCreacion(),
                tema.getUltimaActualizacion(),
                tema.getTipo(),itemList);
    }

}
