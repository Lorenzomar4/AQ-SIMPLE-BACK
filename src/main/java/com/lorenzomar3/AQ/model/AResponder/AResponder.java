package com.lorenzomar3.AQ.model.AResponder;

import com.lorenzomar3.AQ.dto.conversor.AResponseItemDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.model.JerarquiaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Getter
@Setter
public abstract class AResponder {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id ;

    @Column(length = 10500)
    public String titulo;

    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime fechaDeCreacion = LocalDateTime.now();

    @Temporal(TemporalType.TIMESTAMP)
    public LocalDateTime  ultimaActualizacion = LocalDateTime.now();;

    @Enumerated(EnumType.STRING)
    public JerarquiaEnum tipo;

    public AResponder(String titulo) {
        this.titulo = titulo;
    }
    public AResponder() {

    }
    public abstract List<AResponder> contenidoAResponder();

    public abstract boolean contieneCritico();

    public abstract  Integer numeroDePreguntas();

    public abstract void asignarTipoSiSeAgregaDesdeCuestionario();




    public AResponderItemListDTO toResponderItemListDTO(){
       return  AResponseItemDTOConversor.toDTO(this);
    }


}
