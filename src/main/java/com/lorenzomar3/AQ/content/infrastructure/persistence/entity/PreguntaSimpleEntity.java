package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pregunta_simple")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaSimpleEntity extends PreguntaEntity {

    private Boolean respuestaPrecisa;

    @Column(length = 50000)
    private String respuestaEstablecida;
}
