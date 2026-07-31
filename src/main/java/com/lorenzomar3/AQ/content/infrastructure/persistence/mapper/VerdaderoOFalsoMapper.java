package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.VerdaderoOFalso;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.VerdaderoOFalsoEntity;
import org.springframework.stereotype.Component;

@Component
public class VerdaderoOFalsoMapper {

    public VerdaderoOFalso toDomain(VerdaderoOFalsoEntity entity) {
        VerdaderoOFalso verdaderoOFalso = new VerdaderoOFalso();
        verdaderoOFalso.setId(entity.getId());
        verdaderoOFalso.setTitulo(entity.getTitulo());
        verdaderoOFalso.setDescripcion(entity.getDescripcion());
        verdaderoOFalso.setIdDuenio(entity.getIdDuenio());
        verdaderoOFalso.setFechaDeCreacion(entity.getFechaDeCreacion());
        verdaderoOFalso.setTipo(entity.getTipo());
        verdaderoOFalso.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        verdaderoOFalso.setImagenTitulo(entity.getImagenTitulo());
        verdaderoOFalso.setRespuestaVerdadera(entity.getRespuestaVerdadera());
        return verdaderoOFalso;
    }

    public VerdaderoOFalsoEntity toEntity(VerdaderoOFalso domain) {
        VerdaderoOFalsoEntity entity = new VerdaderoOFalsoEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setIdDuenio(domain.getIdDuenio());
        entity.setFechaDeCreacion(domain.getFechaDeCreacion());
        entity.setTipo(domain.getTipo());
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(domain.getIntentosParaQueDejeDeSerCriticoDisponible());
        entity.setImagenTitulo(domain.getImagenTitulo());
        entity.setRespuestaVerdadera(domain.getRespuestaVerdadera());
        return entity;
    }
}
