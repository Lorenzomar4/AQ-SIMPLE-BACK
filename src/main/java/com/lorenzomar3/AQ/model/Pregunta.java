package com.lorenzomar3.AQ.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String pregunta;

    String respuestaTexto;

    String imagen;

    Boolean prioridadDeRepaso;

    Integer descuentoDeRepregunta = 0;

    @OneToMany
    @JoinColumn(name="pregunta")
    List<Palabras> listaDePreguntas =  new ArrayList<>();


}
