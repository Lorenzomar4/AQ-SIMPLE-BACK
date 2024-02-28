package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.DesplegabeIndependiente.DesplegableIndependiente;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface DesplegableIndependienteRepository extends BasePreguntaRepositorio<DesplegableIndependiente> {

    @Override
    @EntityGraph(attributePaths = {"listaDePreguntasConOpcionUnicas.listaDeOpcionesDisponible"})
    Optional<DesplegableIndependiente> findById(Long id);

}
