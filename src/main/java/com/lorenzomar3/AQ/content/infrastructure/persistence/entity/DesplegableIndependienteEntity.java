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
@Table(name = "desplegable_independiente")
@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependienteEntity extends PreguntaEntity {

    // EAGER a diferencia del modelo viejo (LAZY): este repository solo se usa desde los use cases
    // de escritura hexagonales (crear/editar), que necesitan el grafo completo disponible; los
    // endpoints de lectura (fetch/fetch-full) siguen sirviéndose del modelo viejo, sin cambios.
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_pregunta_desplegable_ind")
    private List<SeleccionUnicaParaDesplegableIndependienteEntity> listaDeOpciones = new ArrayList<>();
}
