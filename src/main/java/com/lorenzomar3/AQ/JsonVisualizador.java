package com.lorenzomar3.AQ;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/*

<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
    <version><!-- versión deseada --></version>
</dependency>



 */

public class JsonVisualizador {


    public static void verJson(Object a){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Registrar el módulo JavaTimeModule

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            String json = objectMapper.writeValueAsString(a);
            System.out.println("Json" + json);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
