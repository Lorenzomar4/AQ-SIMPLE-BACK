package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pregunta")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class PreguntaEntity extends AResponderEntity {

    private Integer intentosParaQueDejeDeSerCriticoDisponible;

    @Column(length = 1500)
    private String imagenTitulo;
}
