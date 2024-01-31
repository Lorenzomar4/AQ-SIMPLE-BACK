package com.lorenzomar3.AQ.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

}
