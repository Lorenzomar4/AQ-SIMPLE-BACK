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
                SELECT ar.id,
                       ar.titulo            AS name,
                       ar.fecha_de_creacion AS creationDate,
                       ar.tipo              AS type,
                       CASE
                           WHEN ar.tipo = 'TEMA' OR ar.tipo = 'SUBTEMA' THEN false
                           ELSE p.intentos_para_que_deje_de_ser_critico_disponible != 0
                           END              AS isCritic,
                       CASE
                           WHEN ar.tipo != 'TEMA' AND ar.tipo != 'SUBTEMA' THEN 0
                           ELSE (select count(*) from aresponder  ar2 where id_del_duenio = ar.id and ar2.tipo not in ('TEMA','SUBTEMA') )
                           END              AS numberOfQuestions
                FROM aresponder ar
                         LEFT JOIN pregunta p ON ar.id = p.id
                WHERE ar.id_del_duenio = :id
                ORDER BY ar.fecha_de_creacion DESC;
            """, nativeQuery = true)
    public List<QuestionnaireItem> getIssueItems(@Param("id") Long id); //En un futuro analizar la subconsulta y buscar alternativas mas rapidas


}

