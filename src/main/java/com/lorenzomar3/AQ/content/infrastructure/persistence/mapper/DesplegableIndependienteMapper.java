package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.DesplegableIndependiente;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.DesplegableIndependienteEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class DesplegableIndependienteMapper {

    private final SeleccionUnicaParaDesplegableIndependienteMapper seleccionUnicaParaDesplegableIndependienteMapper;

    public DesplegableIndependienteMapper(SeleccionUnicaParaDesplegableIndependienteMapper seleccionUnicaParaDesplegableIndependienteMapper) {
        this.seleccionUnicaParaDesplegableIndependienteMapper = seleccionUnicaParaDesplegableIndependienteMapper;
    }

    public DesplegableIndependiente toDomain(DesplegableIndependienteEntity entity) {
        DesplegableIndependiente domain = new DesplegableIndependiente();
        domain.setId(entity.getId());
        domain.setTitulo(entity.getTitulo());
        domain.setDescripcion(entity.getDescripcion());
        domain.setIdDuenio(entity.getIdDuenio());
        domain.setFechaDeCreacion(entity.getFechaDeCreacion());
        domain.setTipo(entity.getTipo());
        domain.setIntentosParaQueDejeDeSerCriticoDisponible(entity.getIntentosParaQueDejeDeSerCriticoDisponible());
        domain.setImagenTitulo(entity.getImagenTitulo());
        domain.setListaDeOpciones(
                entity.getListaDeOpciones().stream().map(seleccionUnicaParaDesplegableIndependienteMapper::toDomain).toList()
        );
        return domain;
    }

    public DesplegableIndependienteEntity toEntity(DesplegableIndependiente domain) {
        DesplegableIndependienteEntity entity = new DesplegableIndependienteEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setIdDuenio(domain.getIdDuenio());
        entity.setFechaDeCreacion(domain.getFechaDeCreacion());
        entity.setTipo(domain.getTipo());
        entity.setIntentosParaQueDejeDeSerCriticoDisponible(domain.getIntentosParaQueDejeDeSerCriticoDisponible());
        entity.setImagenTitulo(domain.getImagenTitulo());
        entity.setListaDeOpciones(
                domain.getListaDeOpciones().stream().map(seleccionUnicaParaDesplegableIndependienteMapper::toEntity)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
        return entity;
    }
}
