package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.model.JerarquiaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Getter
@Setter
public abstract class AResponder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id ;

    @Column(length = 10500)
    public String titulo;

    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime fechaDeCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime  ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    public JerarquiaEnum tipo;

    public AResponder() {

    }

    public abstract List<AResponder> contenidoAResponder();

    public abstract boolean contieneCritico();

    public AResponder(String titulo) {
        this.titulo = titulo;
    }
}
