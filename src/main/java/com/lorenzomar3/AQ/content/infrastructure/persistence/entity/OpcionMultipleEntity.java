package com.lorenzomar3.AQ.content.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "opcion_multiple")
@Getter
@Setter
@NoArgsConstructor
public class OpcionMultipleEntity extends PreguntaEntity {

    // EAGER a diferencia del modelo viejo (LAZY): este repository solo se usa desde los use cases
    // de escritura hexagonales (crear/editar), que necesitan la lista siempre disponible; los
    // endpoints de lectura (fetch/fetch-full) siguen sirviéndose del modelo viejo, sin cambios.
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_multiple_opcion")
    private List<OpcionEntity> listaDeOpciones = new ArrayList<>();
}
