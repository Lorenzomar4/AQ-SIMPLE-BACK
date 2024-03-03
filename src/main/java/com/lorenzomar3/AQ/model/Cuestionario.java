package com.lorenzomar3.AQ.model;

import com.lorenzomar3.AQ.dto.conversor.CuestionarioDTOConversor;
import com.lorenzomar3.AQ.dto.conversor.CuestionarioWhitListDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioDTO;
import com.lorenzomar3.AQ.dto.newDto.CuestionarioWithListDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.AsignadorDeTipoALasPreguntas;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.ITemaPregunta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

public class Cuestionario extends AResponder {


    @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime fechaDeCreacion;


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_cuestionario_perteneciente")
    List<AResponder> listaAResponder;

    @PrePersist
    public void beforeSave() {
        this.tipo = TipoAResponder.CUESTIONARIO;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public Cuestionario() {
    }


    public Cuestionario(String titulo) {
        this.titulo = titulo;
        this.fechaDeCreacion = LocalDateTime.now();
        this.listaAResponder = new ArrayList<>();

    }

    public CuestionarioDTO toDTO() {
        return CuestionarioDTOConversor.toDTO(this);
    }

    public CuestionarioWithListDTO toDTOwhitItemList() {
        return CuestionarioWhitListDTOConversor.toDTO(this);
    }

    public void agregarNuevoPreguntaOTema(ITemaPregunta preguntaOTema) {

        AResponder preg = (AResponder) preguntaOTema;

        if (preg instanceof Tema) {
            preg.setTipo(TipoAResponder.TEMA);
        } else {
            preg.setTipo(AsignadorDeTipoALasPreguntas.getInstance().asignarTipo((Pregunta) preg));
        }

        setUltimaActualizacion(LocalDateTime.now());

        listaAResponder.add(preg);
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
        this.tipo = TipoAResponder.CUESTIONARIO;
    }


}
