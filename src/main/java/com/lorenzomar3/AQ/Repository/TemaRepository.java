package com.lorenzomar3.AQ.Repository;

import com.lorenzomar3.AQ.model.Tema;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemaRepository  extends CrudRepository<Tema,Long> {
}
