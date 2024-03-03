package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioWhitItemListDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Tema;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

        if (aResponder instanceof Temario && tipo.equals(TipoAResponder.SUBTEMA)) {
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


    public TemarioBasicDTO toTemarioCuestionarioCardDTO() {
        return TemarioDTOConversor.toTeamarioCuestionarioCardDTO(this);
    }

    public TemarioCuestionarioWhitItemListDTO toTemarioCuestionarioWhitItemList() {
        TemarioCuestionarioWhitItemListDTO temarioCuestionarioWhitItemListDTO =
                new TemarioCuestionarioWhitItemListDTO(id, titulo, fechaDeCreacion);

        List<AResponderItemListDTO> itemList = listaAResponder.stream().map(AResponder::toResponderItemListDTO).toList();

        temarioCuestionarioWhitItemListDTO.setItemList(itemList);


        return temarioCuestionarioWhitItemListDTO;
    }
}
