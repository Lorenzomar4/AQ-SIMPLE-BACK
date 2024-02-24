package com.lorenzomar3.AQ.Repository;


import com.lorenzomar3.AQ.model.AResponder.tipoDePreguntas.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
}
