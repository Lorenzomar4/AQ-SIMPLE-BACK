package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegableCompartido.DesplegableCompartido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DesplegableIndependienteRepository extends BasePreguntaRepositorio<DesplegableIndependiente> {

    @Override
    @EntityGraph(attributePaths = {"listaDeOpcionDesplegableIndependiente.listaDeOpcionesDisponible"})
    Optional<DesplegableIndependiente> findById(Long id);


    @Override
    @EntityGraph(attributePaths = {"listaDeOp cionDesplegableIndependiente.listaDeOpcionesDisponible"})
    @Query("SELECT p FROM DesplegableIndependiente p LEFT JOIN FETCH p.listaDeTeoriaDeLaPregunta WHERE p.id = :preguntaId")
    Optional<DesplegableIndependiente> findByIdWithTeoriaDeLaPregunta(@Param("preguntaId") Long preguntaId);

}
