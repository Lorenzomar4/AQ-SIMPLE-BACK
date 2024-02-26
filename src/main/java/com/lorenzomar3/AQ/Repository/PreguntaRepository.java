package com.lorenzomar3.AQ.Repository;


import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta<?>, Long> {


    @EntityGraph(attributePaths = {"listaDeTeoriaDeLaPregunta" ,"listaDeOpcionesDisponible"})
    public List<Pregunta<?>> findAll();
}
