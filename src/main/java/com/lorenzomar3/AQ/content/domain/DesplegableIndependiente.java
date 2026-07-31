package com.lorenzomar3.AQ.content.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DesplegableIndependiente extends Pregunta {

    private List<SeleccionUnicaParaDesplegableIndependiente> listaDeOpciones = new ArrayList<>();
}
