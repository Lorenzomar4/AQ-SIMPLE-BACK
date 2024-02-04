package com.lorenzomar3.AQ.dto.conversor;

import com.lorenzomar3.AQ.dto.dto.*;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.Tema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CuestionarioConversorDTO {


    public static CuestionarioSimpleDTO toCuestionarioSimpleDTO(Cuestionario cuestionario) {
        return new CuestionarioSimpleDTO(cuestionario.getId(), cuestionario.getNombreCuestionario());
    }


    public static CuestionarioConTemasDTO toCuestionarioConTemas(Cuestionario cuestionario) {

        List<Tema> listaDeTemas = cuestionario.getListaDeTemas();
        List<TemaSinPreguntasDTO> listaDeTemasDTOSinPreguntas = listaDeTemas.stream()
                .map(tema -> TemaConversorDTO.toSimpleDTO(tema)).toList();
        return new CuestionarioConTemasDTO(toCuestionarioSimpleDTO(cuestionario), listaDeTemasDTOSinPreguntas);
    }


    public static Cuestionario simplefromJSON(CuestionarioSimpleDTO cuestionarioSimpleDTO) {
        Long id = cuestionarioSimpleDTO.getId();

        Cuestionario cuestionario = new Cuestionario(cuestionarioSimpleDTO.name);

        if (!Objects.isNull(id)) {
            cuestionario.setId(id);
        }

        return cuestionario;

    }

    public static Cuestionario fromJsonPOST(CuestionarioPostDTO cuestionario) {
        Cuestionario cuestionario1 = simplefromJSON(cuestionario.getQuestionnaire());

            List<Tema>  listaDeTemas = cuestionario.getIssueList().stream().map(issueDto -> {
                return TemaConversorDTO.fromJSONPost(issueDto);
            }).toList();

            listaDeTemas.forEach(tema -> cuestionario1.agregarTema(tema));


        return cuestionario1;
    }

    public static CuestionarioPostDTO toCuestionarioPostDTO(Cuestionario cuestionario) {



        List<TemaPostDTO> temaPostDTOList = cuestionario.getListaDeTemas().stream().map(tema -> {
            System.out.println("my id" +tema.getId());

            return TemaConversorDTO.toDTOPost(tema);
        }).toList();
        return new CuestionarioPostDTO(toCuestionarioSimpleDTO(cuestionario), temaPostDTOList);
    }


}
