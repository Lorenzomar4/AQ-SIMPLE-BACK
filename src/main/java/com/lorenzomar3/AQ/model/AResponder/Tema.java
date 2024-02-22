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

        if (aresponder instanceof Tema) {
            aresponder.setTipo(JerarquiaEnum.HIJO);
        }

        listaDePreguntas.add(aresponder);
    }


}
