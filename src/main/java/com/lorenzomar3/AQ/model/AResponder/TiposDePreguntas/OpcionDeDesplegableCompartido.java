package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class OpcionDeDesplegableCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    Long id;

    @JsonView(View.JustToAnswer.class)
    String pregunta;

    String respuesta;


}
