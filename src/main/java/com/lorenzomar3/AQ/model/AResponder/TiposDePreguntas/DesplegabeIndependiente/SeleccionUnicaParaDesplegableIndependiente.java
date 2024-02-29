package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Verificador.IRespuestaOpcion;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.Opcion;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


//Version minimalista de SeleccionUnica para el DesplegableIndependiente . Esto tiene el objetivo de que la
// query sea mas sencilla para hibernate

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SeleccionUnicaParaDesplegableIndependiente implements IRespuestaOpcion<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    private Long id;

    @JsonView(View.JustToAnswer.class)
    String titulo;


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_desplegable_independiente")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpcionesDisponible = new ArrayList<>();

    public SeleccionUnicaParaDesplegableIndependiente(String titulo, List<Opcion> listaDeOpcionesDisponible) {
        this.titulo = titulo;
        this.listaDeOpcionesDisponible = listaDeOpcionesDisponible;
    }


    @Override
    public Long getRespuestaCorrecta() {
        return listaDeOpcionesDisponible.stream().filter(Opcion::getRespuestaCorrecta).toList().get(0).getId();
    }
}
