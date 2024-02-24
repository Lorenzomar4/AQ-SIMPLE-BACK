package com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Respuesta;
import com.lorenzomar3.AQ.model.AResponder.TeoriaDeLaPregunta;
import com.lorenzomar3.AQ.model.JerarquiaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;


@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class  Pregunta extends AResponder implements  IPregunta{

    public Integer intentosParaQueDejeDeSerCriticoDisponible = 0;

    @Column(length = 1500)
    public String imagenTitulo;

    @OneToMany(fetch = FetchType.LAZY ,orphanRemoval = true)
    @JoinColumn(name ="id_pregunta")
    public List<TeoriaDeLaPregunta> listaDeTeoriaDeLaPregunta;

    @ManyToMany(fetch = FetchType.LAZY)
    public List<Respuesta> opcionDeRespuestas;

    public Pregunta(String titulo, String imagenTitulo) {
        super(titulo);
        this.imagenTitulo = imagenTitulo;
    }

    public Pregunta() {

    }

    @Override
    public List<AResponder> contenidoAResponder() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean contieneCritico() {
        return intentosParaQueDejeDeSerCriticoDisponible > 0;
    }

    @Override
    public Integer numeroDePreguntas() {
        return 0;
    }

    @Override
    public void asignarTipoSiSeAgregaDesdeCuestionario() {
        tipo = JerarquiaEnum.PREGUNTA;
    }



}
