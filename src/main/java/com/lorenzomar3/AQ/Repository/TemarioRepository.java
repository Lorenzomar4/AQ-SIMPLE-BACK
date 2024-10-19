package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.AResponder.Temario.Temario;
import com.lorenzomar3.AQ.model.TipoAResponder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemarioRepository extends JpaRepository<Temario, Long> {


    @EntityGraph(attributePaths = {"listaAResponder"})
    Optional<Temario> findById(Long id);


    @Query("SELECT T from Temario T WHERE T.id = :id")
    Optional<Temario> findByIdEssential(Long id);


    //@Query("SELECT T from Temario T JOIN AResponder AR ON AR.id = T.id WHERE AR.tipo = :tipo")
    List<Temario> findTemarioByTipo( @Param("tipo") TipoAResponder tipoAResponder);
}
