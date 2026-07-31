package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verdaderoofalso")
@Getter
@Setter
@NoArgsConstructor
public class VerdaderoOFalsoEntity extends PreguntaEntity {

    private Boolean respuestaVerdadera;
}
