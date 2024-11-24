package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AResponderRepository extends JpaRepository<AResponder, Long> {

    //Analizar.
    @Query(value = """
                SELECT 
                    ar.id,
                    ar.titulo AS name,
                    ar.fecha_de_creacion AS creationDate,
                    ar.tipo AS type,
                    CASE 
                        WHEN ar.tipo = 'TEMA' THEN false 
                        ELSE p.intentos_para_que_deje_de_ser_critico_disponible != 0 
                    END AS isCritic
                FROM 
                    aresponder ar
                    LEFT JOIN pregunta p ON ar.id = p.id
                WHERE 
                    ar.id_del_duenio = :id
                ORDER BY 
                    ar.fecha_de_creacion DESC
            """, nativeQuery = true)
    public List<QuestionnaireItem> getIssueItems(@Param("id") Long id);


}

