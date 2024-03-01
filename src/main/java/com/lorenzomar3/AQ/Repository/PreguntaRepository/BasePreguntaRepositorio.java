package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@NoRepositoryBean
public interface BasePreguntaRepositorio<T extends AResponder> extends JpaRepository<T, Long> {
    Optional<T> findById(Long id);

    @Query("SELECT p FROM Pregunta p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<T> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);

}