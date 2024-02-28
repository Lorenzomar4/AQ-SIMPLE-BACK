package com.lorenzomar3.AQ.Repository.PreguntaRepository;


import com.lorenzomar3.AQ.model.AResponder.Pregunta;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;


//Para VF Y PreguntaSimple funciona correctamente. Para el resto se queda corto y genera el error de fuera de session aunque se utilice EntityGraph
@Repository
public interface PreguntaRepository  extends BasePreguntaRepositorio<Pregunta> {

    @Override
    @EntityGraph(attributePaths = {"listaDeTeoriaDeLaPregunta"})
    public Optional<Pregunta>  findById(Long id);
}
