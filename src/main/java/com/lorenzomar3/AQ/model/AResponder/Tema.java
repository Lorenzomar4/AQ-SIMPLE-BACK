package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.ITemaPregunta;
import com.lorenzomar3.AQ.model.Cuestionario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
public class Tema extends AResponder implements ITemaPregunta {

    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_tema_duenio")
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
        return listaDePreguntas.stream().anyMatch(aRespond -> aRespond.contieneCritico());
    }


    @Override
    public Integer numeroDePreguntas() {
        return listaDePreguntas
                .stream()
                .filter(a -> a.getTipo().equals(TipoAResponder.PREGUNTA_SIMPLE))
                .toList().size();
    }


    public void asignarTipo(AResponder aResponder) {


        if (aResponder instanceof Cuestionario) {
            aResponder.setTipo(TipoAResponder.CUESTIONARIO);
        }


        if (aResponder instanceof Tema) {
            aResponder.setTipo(TipoAResponder.SUBTEMA);
        }

    }

    @Override
    public void asignacionDeTipo() {
        tipo = TipoAResponder.TEMA;
    }


    public void agregarNuevoPreguntaOTema(ITemaPregunta preguntaOTema) {

        AResponder preg = (AResponder) preguntaOTema;

        if (!getTipo().equals(TipoAResponder.TEMA)) {
            throw new BussinesException("No se permite que se cree una jerarquia mas de temas!");
        }

        if (preg instanceof Tema) {
            preg.setTipo(TipoAResponder.SUBTEMA);
        } else {
            preg.setTipo(AsignadorDeTipoALasPreguntas.getInstance().asignarTipo((Pregunta) preg));
        }

        setUltimaActualizacion(LocalDateTime.now());

        listaDePreguntas.add(preg);
    }

    /*
    public void agregarPreguntaOTema(AResponder aresponder) {
        asignarTipo(aresponder);
        listaDePreguntas.add(aresponder);
        setUltimaActualizacion(LocalDateTime.now());
    }
    */

    public TemaDTO toTemaDTO() {
        return TemaConversorDTO.fullTemaDTO(this);
    }


}
