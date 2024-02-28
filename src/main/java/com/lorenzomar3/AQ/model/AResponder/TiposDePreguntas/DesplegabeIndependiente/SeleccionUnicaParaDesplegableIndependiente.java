package com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.model.AResponder.IPregunta;
import com.lorenzomar3.AQ.model.AResponder.Opcion;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


//Version minimalista de SeleccionUnica para el DesplegableIndependiente . Esto tiene el objetivo de que la
// query sea mas sencilla para hibernate

@Entity
@Getter
@NoArgsConstructor
public class SeleccionUnicaParaDesplegableIndependiente implements IPregunta<Long> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    private Long id;

    @JsonView(View.JustToAnswer.class)
    String titulo;

    public SeleccionUnicaParaDesplegableIndependiente(String titulo, List<Opcion> listaDeOpcionesDisponible) {
        this.titulo = titulo;
        this.listaDeOpcionesDisponible = listaDeOpcionesDisponible;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_desplegable_independiente")
    @JsonView(View.JustToAnswer.class)
    public List<Opcion> listaDeOpcionesDisponible = new ArrayList<>();


    public Long filtrarLaOpcionCorrecta() {
        return listaDeOpcionesDisponible.stream().filter(Opcion::getEsLaOpcionVerdadera).toList().get(0).getId();
    }


    @Override
    public boolean laRespuestaEsCorrecta(Long respuesta) {
        return filtrarLaOpcionCorrecta().equals(respuesta);
    }
}
