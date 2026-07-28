package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "a_responder")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class AResponderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String titulo;

    @Lob
    private String descripcion;

    @Column(name = "id_del_duenio")
    private Long idDuenio;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime fechaDeCreacion;

    @Enumerated(EnumType.STRING)
    private TipoAResponder tipo;
}
