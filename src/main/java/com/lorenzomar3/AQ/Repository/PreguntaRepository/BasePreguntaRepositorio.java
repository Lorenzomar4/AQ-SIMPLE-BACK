package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BasePreguntaRepositorio<T> extends JpaRepository<T, Long> {
    Optional<T> findById(Long id);
}