package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.dto.newDto.TemarioBasicDTO;
import com.lorenzomar3.AQ.model.AResponder.AResponder;
import com.lorenzomar3.AQ.projections.QuestionnaireItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AResponderRepository extends JpaRepository<AResponder, Long> {


    @Query(value =
            "select id ,  titulo as name , fecha_de_creacion as creationDate " +
                    "from aresponder " +
                    "where id_del_duenio = :id ;", nativeQuery = true)
    public List<QuestionnaireItem> getQuestionnaireItem(@Param("id") Long id);


}

