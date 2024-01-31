package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.Cuestionario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
     boolean existsById(Long id);

     @EntityGraph(attributePaths = {"listaDeTemas"})
     Optional<Cuestionario> findById(Long id);


}
