package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SeleccionUnicaRepository extends BasePreguntaRepositorio<SeleccionUnica> {

    @Override
    @EntityGraph(attributePaths = {"listaDeOpcionesConSuRespuestaReal"})
    Optional<SeleccionUnica> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"listaDeOpcionesConSuRespuestaReal"})
    @Query("SELECT p FROM SeleccionUnica p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<SeleccionUnica> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);


}


