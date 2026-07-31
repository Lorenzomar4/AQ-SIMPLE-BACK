package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.Opcion;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.OpcionEntity;
import org.springframework.stereotype.Component;

@Component
public class OpcionMapper {

    public Opcion toDomain(OpcionEntity entity) {
        Opcion opcion = new Opcion();
        opcion.setId(entity.getId());
        opcion.setOpcion(entity.getOpcion());
        opcion.setLaRespuestaEs(entity.getLaRespuestaEs());
        return opcion;
    }

    public OpcionEntity toEntity(Opcion domain) {
        OpcionEntity entity = new OpcionEntity();
        entity.setId(domain.getId());
        entity.setOpcion(domain.getOpcion());
        entity.setLaRespuestaEs(domain.getLaRespuestaEs());
        return entity;
    }
}
