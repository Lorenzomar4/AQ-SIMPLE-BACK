package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido;

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
public class OpcionDeDesplegableCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    Long id;

    @JsonView(View.JustToAnswer.class)
    String pregunta;

    public OpcionDeDesplegableCompartido(String pregunta, String respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    String respuesta;


}