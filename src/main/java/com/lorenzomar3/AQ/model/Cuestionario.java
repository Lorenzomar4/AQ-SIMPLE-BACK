package com.lorenzomar3.AQ.model;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Cuestionario {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id ;
    String nombreCuestionario;


    @OneToMany(fetch = FetchType.LAZY ,cascade =  CascadeType.ALL)
    @JoinColumn(name = "cuestionario_perteneciente")
    List<Tema>  listaDeTemas;


    @OneToMany(fetch = FetchType.LAZY ,cascade =  CascadeType.ALL)
    @JoinColumn(name = "cuestionario_id")
    List<AResponder>  listaAResponder;


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

    public List<Pregunta> todasLasPreguntasQueTieneElCuestionario(){
        List<List<Pregunta>> todosLosTemas = listaDeTemas.stream().map(Tema::getListaDePreguntas).toList();

        return todosLosTemas.stream().flatMap(Collection::stream).collect(Collectors.toList());

    }
}
