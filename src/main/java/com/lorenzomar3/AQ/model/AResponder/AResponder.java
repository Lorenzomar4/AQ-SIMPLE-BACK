package com.lorenzomar3.AQ.model.AResponder;

import com.fasterxml.jackson.annotation.JsonView;
import com.lorenzomar3.AQ.dto.conversor.AResponseItemDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.model.TipoAResponder;
import com.lorenzomar3.AQ.model.View;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class AResponder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(View.JustToAnswer.class)
    public Long id;

    @Column(length = 10500)
    @JsonView(View.JustToAnswer.class)
    public String titulo;


    @Column(length = 10500)
    @JsonView(View.JustToAnswer.class)
    public String descripcion;


    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime fechaDeCreacion = LocalDateTime.now();


    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime ultimaActualizacion = LocalDateTime.now();

    @JsonView(View.JustToAnswer.class)
    @Enumerated(EnumType.STRING)
    public TipoAResponder tipo;


    public AResponder(String titulo) {
        this.titulo = titulo;
    }

    public AResponder(String titulo, TipoAResponder tipo) {
        this.titulo = titulo;
        this.tipo = tipo;
    }

    public AResponder() {

    }

    public abstract List<AResponder> contenidoAResponder();

    public abstract boolean contieneCritico();

    public abstract Integer numeroDePreguntas();

    public abstract void asignacionDeTipo();


    public AResponderItemListDTO toResponderItemListDTO() {
        return AResponseItemDTOConversor.toDTO(this);
    }

}
