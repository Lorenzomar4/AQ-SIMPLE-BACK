package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seleccion_unica_para_desplegable_independiente")
@Getter
@Setter
@NoArgsConstructor
public class SeleccionUnicaParaDesplegableIndependienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    // EAGER a diferencia del modelo viejo (LAZY): este repository solo se usa desde los use cases
    // de escritura hexagonales (crear/editar), que necesitan la lista siempre disponible; los
    // endpoints de lectura (fetch/fetch-full) siguen sirviéndose del modelo viejo, sin cambios.
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_desplegable_independiente")
    private List<OpcionEntity> listaDeOpciones = new ArrayList<>();
}
