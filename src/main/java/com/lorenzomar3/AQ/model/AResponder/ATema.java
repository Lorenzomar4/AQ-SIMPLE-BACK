package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.JerarquiaEnum;
import com.lorenzomar3.AQ.model.Pregunta;
import com.lorenzomar3.AQ.model.Tema;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class ATema extends AResponder {

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "tema_al_que_pertenece")
    public List<AResponder> listaDePreguntas = new ArrayList<>();


    public ATema(String titulo) {
        super(titulo);
    }

    public ATema() {
    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return null;
    }

    @Override
    public boolean contieneCritico() {
        return false;
    }

    public void agregarPreguntaOTema(AResponder aresponder) {

        if (!getTipo().equals(JerarquiaEnum.PADRE)) {
            throw new ErrorDeNegocio("No se permite que se cree una jerarquia mas de temas!");
        }

        if (aresponder instanceof ATema) {
            aresponder.setTipo(JerarquiaEnum.HIJO);
        }

        listaDePreguntas.add(aresponder);
    }


}
