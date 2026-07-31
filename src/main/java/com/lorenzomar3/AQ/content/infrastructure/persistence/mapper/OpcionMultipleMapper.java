package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.OpcionMultiple;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.OpcionMultipleEntity;
import org.springframework.stereotype.Component;

@Component
public class OpcionMultipleMapper {

    private final OpcionMapper opcionMapper;

    public OpcionMultipleMapper(OpcionMapper opcionMapper) {
        this.opcionMapper = opcionMapper;
    }

    public OpcionMultiple toDomain(OpcionMultipleEntity entity) {
        OpcionMultiple opcionMultiple = new OpcionMultiple();
        opcionMultiple.setId(entity.getId());
        opcionMultiple.setTitulo(entity.getTitulo());
        opcionMultiple.setDescripcion(entity.getDescripcion());
        opcionMultiple.setIdDuenio(entity.getIdDuenio());
        opcionMultiple.setFechaDeCreacion(entity.getFechaDeCreacion());
        opcionMultiple.setTipo(entity.getTipo());
        opcionMultiple.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        opcionMultiple.setImagenTitulo(entity.getImagenTitulo());
        opcionMultiple.setListaDeOpciones(
                entity.getListaDeOpciones().stream().map(opcionMapper::toDomain).toList()
        );
        return opcionMultiple;
    }

    public OpcionMultipleEntity toEntity(OpcionMultiple domain) {
        OpcionMultipleEntity entity = new OpcionMultipleEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setIdDuenio(domain.getIdDuenio());
        entity.setFechaDeCreacion(domain.getFechaDeCreacion());
        entity.setTipo(domain.getTipo());
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(domain.getIntentosParaQueDejeDeSerCriticoDisponible());
        entity.setImagenTitulo(domain.getImagenTitulo());
        entity.setListaDeOpciones(
                domain.getListaDeOpciones().stream().map(opcionMapper::toEntity).toList()
        );
        return entity;
    }
}
