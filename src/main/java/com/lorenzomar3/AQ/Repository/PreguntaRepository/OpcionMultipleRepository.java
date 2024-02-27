package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.Repository.PreguntaRepository.BasePreguntaRepositorio;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.OpcionMultiple;
import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface OpcionMultipleRepository extends BasePreguntaRepositorio<OpcionMultiple> {

    @Override
    @EntityGraph(attributePaths = {"listaDeOpciones"})
    Optional<OpcionMultiple> findById(Long id);


}
