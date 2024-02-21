package com.lorenzomar3.AQ.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter

public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    public Long id;

    public Tema( String nombreDeTema) {
        this.nombreDeTema = nombreDeTema;
    }

    public Tema(String nombreDeTema, List<Pregunta> listaDePreguntas) {
        this.nombreDeTema = nombreDeTema;
        this.listaDePreguntas = listaDePreguntas;
    }

    public String nombreDeTema;


    @OneToMany(orphanRemoval = true , fetch = FetchType.LAZY , cascade =  CascadeType.ALL)
    @JoinColumn(name = "tema")
    public List<Pregunta> listaDePreguntas =new ArrayList<>() ;


    public void agregarPregunta(Pregunta pregunta){
        listaDePreguntas.add(pregunta);
    }

    public void setId(Long id ){
        this.id = id;
    }


    public Tema() {}

    public void setNombreDeTema(String nombreDeTema) {
        this.nombreDeTema = nombreDeTema;
    }
}
