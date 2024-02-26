package com.lorenzomar3.AQ.model.AResponder;


import jakarta.persistence.*;

@Entity
public class TeoriaDeLaPregunta  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(length = 10500)
    public String respuesta;
    @Column(length = 1000)
    public String imagen;

}
