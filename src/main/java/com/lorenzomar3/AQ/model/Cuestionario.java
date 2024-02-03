package com.lorenzomar3.AQ.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Cuestionario {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id ;
    String nombreCuestionario;


    @OneToMany(fetch = FetchType.LAZY ,cascade =  CascadeType.PERSIST)
    @JoinColumn(name = "cuestionario_perteneciente")
    List<Tema>  listaDeTemas;


    public Cuestionario(String nombreCuestionario) {
        this.nombreCuestionario = nombreCuestionario;
        this.listaDeTemas = new ArrayList<>();
    }



    public void setId(Long id ){
        this.id = id;

    }

    public void agregarTema(Tema tema){
        listaDeTemas.add(tema);
    }
}
