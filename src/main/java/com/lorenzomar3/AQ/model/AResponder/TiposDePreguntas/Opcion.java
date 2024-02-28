package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
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
    String respuestaCorrecta;



    public Boolean esLaOpcionVerdadera;

    public Opcion(String respuestaCorrecta, Boolean esLaOpcionVerdadera) {
        this.respuestaCorrecta = respuestaCorrecta;
        this.esLaOpcionVerdadera = esLaOpcionVerdadera;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Boolean getRespuestaCorrecta() {
        return esLaOpcionVerdadera;
    }
}
