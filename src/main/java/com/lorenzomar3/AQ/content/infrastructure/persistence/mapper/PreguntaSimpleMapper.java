package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.PreguntaSimple;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.PreguntaSimpleEntity;
import org.springframework.stereotype.Component;

@Component
public class PreguntaSimpleMapper {

    public PreguntaSimple toDomain(PreguntaSimpleEntity entity) {
        PreguntaSimple preguntaSimple = new PreguntaSimple();
        preguntaSimple.setId(entity.getId());
        preguntaSimple.setTitulo(entity.getTitulo());
        preguntaSimple.setDescripcion(entity.getDescripcion());
        preguntaSimple.setIdDuenio(entity.getIdDuenio());
        preguntaSimple.setFechaDeCreacion(entity.getFechaDeCreacion());
        preguntaSimple.setTipo(entity.getTipo());
        preguntaSimple.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        preguntaSimple.setImagenTitulo(entity.getImagenTitulo());
        preguntaSimple.setRespuestaEstablecida(entity.getRespuestaEstablecida());
        preguntaSimple.setRespuestaPrecisa(entity.getRespuestaPrecisa());
        return preguntaSimple;
    }

    public PreguntaSimpleEntity toEntity(PreguntaSimple domain) {
        PreguntaSimpleEntity entity = new PreguntaSimpleEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setIdDuenio(domain.getIdDuenio());
        entity.setFechaDeCreacion(domain.getFechaDeCreacion());
        entity.setTipo(domain.getTipo());
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(domain.getIntentosParaQueDejeDeSerCriticoDisponible());
        entity.setImagenTitulo(domain.getImagenTitulo());
        entity.setRespuestaEstablecida(domain.getRespuestaEstablecida());
        entity.setRespuestaPrecisa(domain.getRespuestaPrecisa());
        return entity;
    }
}
