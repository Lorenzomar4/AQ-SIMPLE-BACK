package com.lorenzomar3.AQ.model;

import com.lorenzomar3.AQ.dto.conversor.CuestionarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter

public class Cuestionario {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id ;
    String nombreCuestionario;


    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime fechaDeCreacion;


    @OneToMany(fetch = FetchType.LAZY ,cascade =  CascadeType.ALL)
    @JoinColumn(name = "cuestionario_id")
    List<AResponder>  listaAResponder;


    public void setId(Long id ){
        this.id = id;
    }

    public Cuestionario() {
    }

    public Cuestionario(String nombreCuestionario) {
        this.nombreCuestionario = nombreCuestionario;
        this.fechaDeCreacion = LocalDateTime.now();

    }

    public CuestionarioDTO toDTO() {
        return CuestionarioDTOConversor.toDTO(this);
    }



}
