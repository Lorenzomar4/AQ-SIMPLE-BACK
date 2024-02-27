package com.lorenzomar3.AQ.model.AResponder;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Opcion {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    Long id;

    @JsonView(View.JustToAnswer.class)
    String respuesta;

    public Boolean esLaOpcionVerdadera;

    public Opcion(String respuesta, Boolean esLaOpcionVerdadera) {
        this.respuesta = respuesta;
        this.esLaOpcionVerdadera = esLaOpcionVerdadera;
    }
}
