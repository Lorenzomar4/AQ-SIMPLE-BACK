package com.lorenzomar3.AQ.content.infrastructure.persistence.repository;

import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.TemarioEntity;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemarioJpaRepository extends BaseContentRepositorio<TemarioEntity> {

    List<TemarioEntity> findByTipo(@Param("tipo") TipoAResponder tipo);
}
