package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerIdsCriticosUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ObtenerIdsCriticosService implements ObtenerIdsCriticosUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public ObtenerIdsCriticosService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> obtenerIdsCriticos(Long id) {
        List<Long> ids = new ArrayList<>(temarioRepositoryPort.findCriticalQuestionIds(id));
        Collections.shuffle(ids);
        return ids;
    }
}
