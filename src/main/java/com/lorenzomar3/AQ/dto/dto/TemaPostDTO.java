package com.lorenzomar3.AQ.dto.dto;

import lombok.Data;

import java.util.List;

@Data
public class TemaPostDTO {

    Long id;

    String name;

    public List<PreguntaPostDTO> preguntaPostDTOList;



}
