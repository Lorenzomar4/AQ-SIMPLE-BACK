package com.lorenzomar3.AQ.model.AResponder;


import jakarta.persistence.*;
import lombok.Getter;


@Entity
@Getter
public class Respuesta {



    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    public Long id;
    public String respuestaPropuesta;
    public Boolean esLaRespuestaVerdadera;





}
