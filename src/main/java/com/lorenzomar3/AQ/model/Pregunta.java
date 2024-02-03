package com.lorenzomar3.AQ.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String pregunta;

    String respuestaTexto;

    String imagen;



    Boolean prioridadDeRepaso = false;

    Integer descuentoDeRepregunta = 0;

    @ElementCollection
    List<String> listaDePreguntas =  new ArrayList<>();


    public Pregunta(String pregunta, String respuestaTexto) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
    }

    public Pregunta(String pregunta, String respuestaTexto, List<String> listaDePreguntas) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
        this.listaDePreguntas = listaDePreguntas;
    }

    public void  setId(Long id){
        this.id = id;

    }

    public Pregunta() {

    }
}
