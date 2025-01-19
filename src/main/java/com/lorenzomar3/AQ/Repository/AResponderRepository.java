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
                WITH RECURSIVE
                                                      PREGUNTAS_DEL_TEMA AS (SELECT ar.ID,
                                                                                    ar.id_del_duenio,
                                                                                    ar.titulo,
                                                                                    ar.fecha_de_creacion,
                                                                                    ar.tipo,
                                                                                    2       as nivel,
                                                                                    CASE
                                                                                        WHEN pre.intentos_para_que_deje_de_ser_critico_disponible = 0 THEN FALSE
                                                                                        ELSE TRUE
                                                                                        END AS isCritic
                                                                             FROM aresponder ar
                                                                                      INNER JOIN
                                                                                  pregunta pre ON pre.ID = ar.ID
                                                                             WHERE ar.id_del_duenio = :id),
                                                      TODO_EL_CONTENIDO_DEL_TEMA AS (SELECT ID,
                                                                                            id_del_duenio,
                                                                                            titulo,
                                                                                            fecha_de_creacion,
                                                                                            tipo,
                                                                                            1 as nivel
                                                                                     FROM aresponder
                                                                                     WHERE id = :id
                                                                                     UNION ALL
                                                                                     SELECT ar.ID,
                                                                                            ar.id_del_duenio,
                                                                                            ar.titulo,
                                                                                            ar.fecha_de_creacion,
                                                                                            ar.tipo,
                                                                                            nivel + 1
                                                                                     FROM aresponder ar
                                                                                              INNER JOIN
                                                                                          TODO_EL_CONTENIDO_DEL_TEMA sp ON sp.id = ar.id_del_duenio),
                                                      SOLO_TEMAS AS (SELECT ID,
                                                                            id_del_duenio,
                                                                            titulo,
                                                                            fecha_de_creacion,
                                                                            tipo,
                                                                            nivel
                                                                     FROM TODO_EL_CONTENIDO_DEL_TEMA
                                                                     WHERE TIPO IN ('CUESTIONARIO', 'TEMA', 'SUBTEMA')),
                                                      SOLO_PREGUNTAS_PERTENECIENTES_AL_TEMA AS (SELECT tct.ID,
                                                                                                       tct.id_del_duenio,
                                                                                                       tct.titulo,
                                                                                                       tct.fecha_de_creacion,
                                                                                                       tct.tipo,
                                                                                                       tct.nivel,
                                                                                                       CASE
                                                                                                           WHEN pre.intentos_para_que_deje_de_ser_critico_disponible = 0
                                                                                                               THEN FALSE
                                                                                                           ELSE TRUE
                                                                                                           END AS isCritic
                                                                                                FROM TODO_EL_CONTENIDO_DEL_TEMA tct
                                                                                                         INNER JOIN
                                                                                                     pregunta pre ON pre.id = tct.id),
                                                      TEMAS_CON_CRITICOS AS (SELECT st.ID,
                                                                                    st.id_del_duenio,
                                                                                    st.titulo,
                                                                                    st.fecha_de_creacion,
                                                                                    st.tipo,
                                                                                    st.nivel,
                                                                                    BOOL_OR(COALESCE(spre.isCritic, FALSE)) AS isCritic
                                                                             FROM SOLO_TEMAS st
                                                                                      LEFT JOIN
                                                                                  SOLO_PREGUNTAS_PERTENECIENTES_AL_TEMA spre ON st.ID = spre.id_del_duenio
                                                                             WHERE st.nivel = 2
                                                                             GROUP BY st.ID,
                                                                                      st.id_del_duenio,
                                                                                      st.titulo,
                                                                                      st.fecha_de_creacion,
                                                                                      st.tipo,
                                                                                      st.nivel),
                                                      UNION_TABLAS AS (SELECT *
                                                                       FROM PREGUNTAS_DEL_TEMA
                                                                       UNION ALL
                                                                       SELECT *
                                                                       FROM TEMAS_CON_CRITICOS
                                                                       ORDER BY ID ASC)
                                                  SELECT id, titulo as name , fecha_de_creacion as creationDate , tipo as type , isCritic , CASE
                                                                             WHEN UT.tipo != 'TEMA' AND UT.tipo != 'SUBTEMA' THEN 0
                                                                             ELSE (select count(*) from aresponder  ar2 where id_del_duenio = UT.id and ar2.tipo not in ('TEMA','SUBTEMA') )
                                                                             END              AS numberOfQuestions
                                                  FROM UNION_TABLAS UT;
            
            
            """, nativeQuery = true)
    public List<QuestionnaireItem> getIssueItems(@Param("id") Long id); //En un futuro analizar la subconsulta y buscar alternativas mas rapidas


}

