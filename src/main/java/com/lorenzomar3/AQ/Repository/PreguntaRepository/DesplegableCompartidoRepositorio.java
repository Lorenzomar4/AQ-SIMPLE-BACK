package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DesplegableCompartidoRepositorio  extends BasePreguntaRepositorio<DesplegableCompartido> {

    @Override
    @EntityGraph(attributePaths = {"listaDeOpciones"})
    Optional<DesplegableCompartido> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"listaDeOpciones"})
    @Query("SELECT p FROM DesplegableCompartido p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<DesplegableCompartido> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);
}
