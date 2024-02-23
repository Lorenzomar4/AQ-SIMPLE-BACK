package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;

public class AResponseItemDTOConversor {


    public static  AResponderItemListDTO toDTO(AResponder aResponder){

        return new AResponderItemListDTO(aResponder.getId(),aResponder.getTitulo(),aResponder.getTipo() ,
                aResponder.numeroDePreguntas(),aResponder.contieneCritico());

    }






}
