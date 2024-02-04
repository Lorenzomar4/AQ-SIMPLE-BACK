package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuestionarioRepositoryFull extends JpaRepository<Cuestionario, Long> {


    @EntityGraph(attributePaths = {"listaDeTemas.listaDePreguntas.listaDePalabras"})
    Optional<Cuestionario> findById(Long id);

}
