package com.lorenzomar3.AQ.content.infrastructure.persistence.repository;

import com.lorenzomar3.AQ.content.infrastructure.persistence.entity.AResponderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseContentRepositorio<T extends AResponderEntity> extends JpaRepository<T, Long> {
}
