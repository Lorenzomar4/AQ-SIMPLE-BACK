package com.lorenzomar3.AQ.model.AResponder;


import jakarta.persistence.*;

@Entity

public class TeoriaDeLaPregunta  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    Pregunta<?> pregunaDuenia;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(length = 10500)
    public String respuesta;
    @Column(length = 1000)
    public String imagen;

}
