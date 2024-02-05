package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.Tema;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemaRepository  extends CrudRepository<Tema,Long> {


    @Override
    @EntityGraph(attributePaths = {"listaDePreguntas.listaDePalabras"})
    Optional<Tema> findById(Long aLong);
}
