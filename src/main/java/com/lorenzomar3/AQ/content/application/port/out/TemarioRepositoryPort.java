package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.Temario;

import java.util.List;
import java.util.Optional;

public interface TemarioRepositoryPort {

    List<Temario> findAllCuestionarios();

    Optional<Temario> findById(Long id);

    Temario save(Temario temario);

    void deleteById(Long id);

    List<AResponderChildRef> findDirectChildren(Long id);
}
