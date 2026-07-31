package com.lorenzomar3.AQ.content.infrastructure.persistence.mapper;

import com.lorenzomar3.AQ.content.domain.SeleccionUnicaParaDesplegableIndependiente;
import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.SeleccionUnicaParaDesplegableIndependienteEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class SeleccionUnicaParaDesplegableIndependienteMapper {

    private final OpcionMapper opcionMapper;

    public SeleccionUnicaParaDesplegableIndependienteMapper(OpcionMapper opcionMapper) {
        this.opcionMapper = opcionMapper;
    }

    public SeleccionUnicaParaDesplegableIndependiente toDomain(SeleccionUnicaParaDesplegableIndependienteEntity entity) {
        SeleccionUnicaParaDesplegableIndependiente domain = new SeleccionUnicaParaDesplegableIndependiente();
        domain.setId(entity.getId());
        domain.setTitulo(entity.getTitulo());
        domain.setListaDeOpciones(
                entity.getListaDeOpciones().stream().map(opcionMapper::toDomain).toList()
        );
        return domain;
    }

    public SeleccionUnicaParaDesplegableIndependienteEntity toEntity(SeleccionUnicaParaDesplegableIndependiente domain) {
        SeleccionUnicaParaDesplegableIndependienteEntity entity = new SeleccionUnicaParaDesplegableIndependienteEntity();
        entity.setId(domain.getId());
        entity.setTitulo(domain.getTitulo());
        entity.setListaDeOpciones(
                domain.getListaDeOpciones().stream().map(opcionMapper::toEntity)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
        return entity;
    }
}
