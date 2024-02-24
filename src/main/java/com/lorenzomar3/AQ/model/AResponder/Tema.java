package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.dto.conversor.TemaConversorDTO;
import com.lorenzomar3.AQ.dto.newDto.TemaDTO;
import com.lorenzomar3.AQ.exception.ErrorDeNegocio;
import com.lorenzomar3.AQ.model.JerarquiaEnum;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
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
        return listaDePreguntas.stream().anyMatch(aRespond -> aRespond.contieneCritico());
    }


    @Override
    public Integer numeroDePreguntas() {
        return listaDePreguntas
                .stream()
                .filter(a -> a.getTipo().equals(JerarquiaEnum.PREGUNTA))
                .toList().size();
    }

    @Override
    public void asignarTipoSiSeAgregaDesdeCuestionario() {
        tipo = JerarquiaEnum.TEMA;
    }

    public void agregarPreguntaOTema(AResponder aresponder) {

        if (!getTipo().equals(JerarquiaEnum.TEMA)) {
            throw new ErrorDeNegocio("No se permite que se cree una jerarquia mas de temas!");
        }

        if (aresponder instanceof Tema) {
            aresponder.setTipo(JerarquiaEnum.SUBTEMA);
        }

        listaDePreguntas.add(aresponder);
        setUltimaActualizacion(LocalDateTime.now());
    }


    public TemaDTO toTemaDTO() {
        return TemaConversorDTO.fullTemaDTO(this);
    }


}
