package com.lorenzomar3.AQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonVisualizador {


    public static void verJson(Object a){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            String json = objectMapper.writeValueAsString(a);
            System.out.println("Json" + json);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
