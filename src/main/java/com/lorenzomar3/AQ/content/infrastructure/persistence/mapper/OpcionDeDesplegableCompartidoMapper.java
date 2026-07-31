package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.OpcionDeDesplegableCompartido;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.OpcionDeDesplegableCompartidoEntity;
import org.springframework.stereotype.Component;

@Component
public class OpcionDeDesplegableCompartidoMapper {

    public OpcionDeDesplegableCompartido toDomain(OpcionDeDesplegableCompartidoEntity entity) {
        OpcionDeDesplegableCompartido opcion = new OpcionDeDesplegableCompartido();
        opcion.setId(entity.getId());
        opcion.setPregunta(entity.getPregunta());
        opcion.setRespuesta(entity.getRespuesta());
        return opcion;
    }

    public OpcionDeDesplegableCompartidoEntity toEntity(OpcionDeDesplegableCompartido domain) {
        OpcionDeDesplegableCompartidoEntity entity = new OpcionDeDesplegableCompartidoEntity();
        entity.setId(domain.getId());
        entity.setPregunta(domain.getPregunta());
        entity.setRespuesta(domain.getRespuesta());
        return entity;
    }
}
