package com.lorenzomar3.AQ.model.AResponder;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TeoriaDeLaPregunta {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    Pregunta pregunaDuenia;

    @Id
    @JsonView(View.Full.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JsonView(View.Full.class)
    @Column(length = 10500)
    public String respuesta;

    public TeoriaDeLaPregunta(String respuesta) {
        this.respuesta = respuesta;
    }

    @JsonView(View.Full.class)
    @Column(length = 1000)
    public String imagen;

}
