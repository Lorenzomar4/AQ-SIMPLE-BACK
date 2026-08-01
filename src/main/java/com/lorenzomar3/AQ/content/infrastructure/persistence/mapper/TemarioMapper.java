package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.Temario;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.TemarioEntity;
import org.springframework.stereotype.Component;

@Component
public class TemarioMapper {

    public Temario toDomain(TemarioEntity entity) {
        Temario temario = new Temario();
        temario.setId(entity.getId());
        temario.setTitulo(entity.getTitulo());
        temario.setDescripcion(entity.getDescripcion());
        temario.setIdDuenio(entity.getIdDuenio());
        temario.setFechaDeCreacion(entity.getFechaDeCreacion());
        temario.setUltimaActualizacion(entity.getUltimaActualizacion());
        temario.setTipo(entity.getTipo());
        return temario;
    }

    public TemarioEntity toEntity(Temario temario) {
        TemarioEntity entity = new TemarioEntity();
        entity.setId(temario.getId());
        entity.setTitulo(temario.getTitulo());
        entity.setDescripcion(temario.getDescripcion());
        entity.setIdDuenio(temario.getIdDuenio());
        entity.setFechaDeCreacion(temario.getFechaDeCreacion());
        entity.setUltimaActualizacion(temario.getUltimaActualizacion());
        entity.setTipo(temario.getTipo());
        return entity;
    }
}
