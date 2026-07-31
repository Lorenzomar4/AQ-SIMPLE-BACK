package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.DesplegableCompartido;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.DesplegableCompartidoEntity;
import org.springframework.stereotype.Component;

@Component
public class DesplegableCompartidoMapper {

    private final OpcionDeDesplegableCompartidoMapper opcionDeDesplegableCompartidoMapper;

    public DesplegableCompartidoMapper(OpcionDeDesplegableCompartidoMapper opcionDeDesplegableCompartidoMapper) {
        this.opcionDeDesplegableCompartidoMapper = opcionDeDesplegableCompartidoMapper;
    }

    public DesplegableCompartido toDomain(DesplegableCompartidoEntity entity) {
        DesplegableCompartido desplegableCompartido = new DesplegableCompartido();
        desplegableCompartido.setId(entity.getId());
        desplegableCompartido.setTitulo(entity.getTitulo());
        desplegableCompartido.setDescripcion(entity.getDescripcion());
        desplegableCompartido.setIdDuenio(entity.getIdDuenio());
        desplegableCompartido.setFechaDeCreacion(entity.getFechaDeCreacion());
        desplegableCompartido.setTipo(entity.getTipo());
        desplegableCompartido.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        desplegableCompartido.setImagenTitulo(entity.getImagenTitulo());
        desplegableCompartido.setListaDeOpciones(
                entity.getListaDeOpciones().stream().map(opcionDeDesplegableCompartidoMapper::toDomain).toList()
        );
        return desplegableCompartido;
    }

    public DesplegableCompartidoEntity toEntity(DesplegableCompartido domain) {
        DesplegableCompartidoEntity entity = new DesplegableCompartidoEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setIdDuenio(domain.getIdDuenio());
        entity.setFechaDeCreacion(domain.getFechaDeCreacion());
        entity.setTipo(domain.getTipo());
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(domain.getIntentosParaQueDejeDeSerCriticoDisponible());
        entity.setImagenTitulo(domain.getImagenTitulo());
        entity.setListaDeOpciones(
                domain.getListaDeOpciones().stream().map(opcionDeDesplegableCompartidoMapper::toEntity).toList()
        );
        return entity;
    }
}
