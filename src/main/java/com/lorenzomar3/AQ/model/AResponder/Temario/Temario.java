package com.lorenzomar3.AQ.model.AResponder.Temario;

import com.lorenzomar3.AQ.dto.conversor.TemarioDTOConversor;
import com.lorenzomar3.AQ.dto.newDto.AResponderItemListDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.dto.newDto.TemarioCuestionarioWhitItemListDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.TipoAResponder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    }

    public Temario(String titulo) {
        super(titulo);
    }

    @PostLoad
    public void init() {

        if (tipo.equals(TipoAResponder.CUESTIONARIO)) {
            tipoDeTemario = TipoCuestionario.getInstance();
        }else {
            tipoDeTemario = TipoTema.getInstance();
        }

    }

    public void agregarALaLista(AResponder aResponder) {

        TipoAResponder elTipoAsignable;

        if (aResponder instanceof Temario && tipo.equals(TipoAResponder.SUBTEMA)) {
            throw new BussinesException("No se puede agregar un subtema a otro subtema");
        }

        elTipoAsignable = tipoDeTemario.retornarElTipoParaLaPreguntaOTemario(aResponder);



        aResponder.setTipo(elTipoAsignable);

        System.out.println("tipo asignado : "+aResponder.getTipo());

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

    @Override
    public List<Long> obtenerListaDeIdentificadoresDePreguntas() {
        return listaAResponder.stream().flatMap(elem -> elem.obtenerListaDeIdentificadoresDePreguntas().stream()).toList();
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
