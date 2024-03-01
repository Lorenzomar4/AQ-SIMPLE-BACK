package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.BasePreguntaRepositorio;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OpcionMultipleRepository extends BasePreguntaRepositorio<OpcionMultiple> {

    @Override
    @EntityGraph(attributePaths = {"listaDeOpcionesConSuRespuestaReal"})
    Optional<OpcionMultiple> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"listaDeOpcionesConSuRespuestaReal"})
    @Query("SELECT p FROM OpcionMultiple p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<OpcionMultiple> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);


}
