package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.AResponder.TipoTemarioEnum;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class Temario extends AResponder {


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_del_duenio")
    List<AResponder> listaAResponder = new ArrayList<>();

    @Transient
    TipoDeTemario tipoDeTemario;


    public Temario(String titulo, TipoAResponder tipo) {
        super(titulo, tipo);
        init();
    }

    public Temario(String titulo) {
        super(titulo);
    }

    @PostLoad
    void init() {

        if (tipo.equals(TipoAResponder.CUESTIONARIO)) {
            tipoDeTemario = TipoCuestionario.getInstance();
        } else if (tipo.equals(TipoAResponder.TEMA)) {
            tipoDeTemario = TipoTema.getInstance();
        } else {
            tipoDeTemario = null;
        }

    }

    public void agregarALaLista(AResponder aResponder) {

        if (aResponder instanceof Tema && tipo.equals(TipoAResponder.SUBTEMA)) {
            throw new BussinesException("No se puede agregar un subtema a otro subtema");
        }

        aResponder.setTipo(tipoDeTemario.retornarElTipoParaLaPreguntaOTemario(aResponder));

        listaAResponder.add(aResponder);
    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return null;
    }

    @Override
    public boolean contieneCritico() {
        return false;
    }

    @Override
    public Integer numeroDePreguntas() {
        return null;
    }

    @Override
    public void asignacionDeTipo() {

    }
}
