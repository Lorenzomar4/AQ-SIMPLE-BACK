package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter

@NoArgsConstructor
public class Opcion implements IRespuestaOpcion<Boolean> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    Long id;

    @JsonView(View.JustToAnswer.class)
    String opcion;

    @JsonView(View.Full.class)
    public Boolean laRespuestaEs;

    public Opcion(String respuestaCorrecta, Boolean laRespuestaEs) {
        this.opcion = respuestaCorrecta;
        this.laRespuestaEs = laRespuestaEs;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Boolean getRespuestaCorrecta() {
        return laRespuestaEs;
    }
}
