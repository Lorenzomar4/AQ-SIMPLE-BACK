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
        temario.setTipo(entity.getTipo());
        return temario;
    }
}
