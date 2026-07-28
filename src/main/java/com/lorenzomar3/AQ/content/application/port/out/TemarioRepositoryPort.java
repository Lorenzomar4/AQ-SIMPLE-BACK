package com.lorenzomar3.AQ.content.application.port.out;

import com.lorenzomar3.AQ.content.domain.Temario;

import java.util.List;

public interface TemarioRepositoryPort {

    List<Temario> findAllCuestionarios();
}
