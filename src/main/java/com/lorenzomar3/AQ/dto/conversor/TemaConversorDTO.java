package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.TemaPostDTO;
import com.lorenzomar3.AQ.model.AResponder.Tema;

public class TemaConversorDTO {

    public static Tema fromJSON(TemaPostDTO temaPostDTO){
        Tema tema = new Tema();

        tema.setId(temaPostDTO.getId());
        tema.setTitulo(temaPostDTO.getName());
        return tema;
    }
}
