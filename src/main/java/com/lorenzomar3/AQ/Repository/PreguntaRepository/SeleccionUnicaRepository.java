package com.lorenzomar3.AQ.Repository.PreguntaRepository;

import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import com.lorenzomar3.AQ.model.AResponder.TiposDePreguntas.SeleccionUnica;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
    public interface SeleccionUnicaRepository extends BasePreguntaRepositorio<SeleccionUnica>{

        @Override
        @EntityGraph(attributePaths = {"listaDeTeoriaDeLaPregunta" ,"listaDeOpcionesDisponible"})
        Optional<SeleccionUnica> findById(Long id);
    }


