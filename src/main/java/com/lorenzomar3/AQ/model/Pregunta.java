package com.lorenzomar3.AQ.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 1500)
    String pregunta;

    @Column(length = 1500)
    String respuestaTexto;



    Integer descuentoDeRepregunta = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    List<String> listaDePalabras =  new ArrayList<>();

    @Column(length = 1500)
    String imagen;

    @Column(length = 1500)
    String imagenRespuesta;


    public Pregunta(String pregunta, String respuestaTexto) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
    }

    public Pregunta(String pregunta, String respuestaTexto, List<String> listaDePalabras) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
        this.listaDePalabras = listaDePalabras;
    }

    public Pregunta(String pregunta, String respuestaTexto, List<String> listaDePalabras, String imagen) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
        this.listaDePalabras = listaDePalabras;
        this.imagen = imagen;
    }

    public Pregunta(String pregunta, String respuestaTexto, List<String> listaDePalabras, String imagen, String imagenRespuesta) {
        this.pregunta = pregunta;
        this.respuestaTexto = respuestaTexto;
        this.listaDePalabras = listaDePalabras;
        this.imagen = imagen;
        this.imagenRespuesta = imagenRespuesta;
    }

    public void  setId(Long id){
        this.id = id;

    }

    public Pregunta() {

    }

    public void descontarRepregunta(){


        if(descuentoDeRepregunta>=1){
            descuentoDeRepregunta=descuentoDeRepregunta-1;
        }
    }

    public void equivocacion(){
        descuentoDeRepregunta = 3;
        System.out.println("deberia modificarse");
    }


}
