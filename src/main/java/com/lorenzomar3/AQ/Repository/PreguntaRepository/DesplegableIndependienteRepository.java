package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DesplegableIndependienteRepository extends BasePreguntaRepositorio<DesplegableIndependiente> {

    @Override
    @EntityGraph(attributePaths = {"listaDePreguntasConOpcionUnicas.listaDeOpcionesDisponible"})
    Optional<DesplegableIndependiente> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"listaDePreguntasConOpcionUnicas"})
    @Query("SELECT p FROM Pregunta p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<DesplegableIndependiente> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);

}
