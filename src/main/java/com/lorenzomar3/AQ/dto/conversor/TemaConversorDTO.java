package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.TemaSinPreguntasDTO;
import com.lorenzomar3.AQ.model.Tema;

public class TemaConversorDTO {

    public static TemaSinPreguntasDTO toSimpleDTO(Tema tema){

        return new TemaSinPreguntasDTO(tema.getId() , tema.getNombreDeTema());

    }


    public static Tema fromJson(TemaSinPreguntasDTO  tema){
        return new Tema(tema.getName());

    }
}
