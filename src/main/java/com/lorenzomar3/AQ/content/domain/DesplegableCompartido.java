package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DesplegableCompartido extends Pregunta {

    private List<OpcionDeDesplegableCompartido> listaDeOpciones = new ArrayList<>();
}
