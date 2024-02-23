package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.JerarquiaEnum;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;



@Entity
public class Tema extends AResponder {

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "tema_al_que_pertenece")
    public List<AResponder> listaDePreguntas = new ArrayList<>();


    public Tema(String titulo) {
        super(titulo);
    }

    public Tema() {
    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return listaDePreguntas;
    }

    @Override
    public boolean contieneCritico() {
        return listaDePreguntas.stream().anyMatch(aRespond ->  aRespond.contieneCritico());
    }



    @Override
    public Integer numeroDePreguntas() {
      return  listaDePreguntas
                .stream()
                .filter(a->a.getTipo().equals(JerarquiaEnum.PREGUNTA))
                .toList().size();
    }

    public void agregarPreguntaOTema(AResponder aresponder) {

        if (!getTipo().equals(JerarquiaEnum.TEMAPADRE)) {
            throw new ErrorDeNegocio("No se permite que se cree una jerarquia mas de temas!");
        }

        if (aresponder instanceof Tema) {
            aresponder.setTipo(JerarquiaEnum.TEMAHIJO);
        }

        listaDePreguntas.add(aresponder);
    }



}
