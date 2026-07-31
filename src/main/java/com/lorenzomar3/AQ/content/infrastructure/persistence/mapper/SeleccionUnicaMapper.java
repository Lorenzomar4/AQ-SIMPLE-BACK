package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.SeleccionUnica;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.SeleccionUnicaEntity;
import org.springframework.stereotype.Component;

@Component
public class SeleccionUnicaMapper {

    private final OpcionMapper opcionMapper;

    public SeleccionUnicaMapper(OpcionMapper opcionMapper) {
        this.opcionMapper = opcionMapper;
    }

    public SeleccionUnica toDomain(SeleccionUnicaEntity entity) {
        SeleccionUnica seleccionUnica = new SeleccionUnica();
        seleccionUnica.setId(entity.getId());
        seleccionUnica.setTitulo(entity.getTitulo());
        seleccionUnica.setDescripcion(entity.getDescripcion());
        seleccionUnica.setIdDuenio(entity.getIdDuenio());
        seleccionUnica.setFechaDeCreacion(entity.getFechaDeCreacion());
        seleccionUnica.setTipo(entity.getTipo());
        seleccionUnica.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        seleccionUnica.setImagenTitulo(entity.getImagenTitulo());
        seleccionUnica.setListaDeOpciones(
                entity.getListaDeOpciones().stream().map(opcionMapper::toDomain).toList()
        );
        return seleccionUnica;
    }

    public SeleccionUnicaEntity toEntity(SeleccionUnica domain) {
        SeleccionUnicaEntity entity = new SeleccionUnicaEntity();
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
