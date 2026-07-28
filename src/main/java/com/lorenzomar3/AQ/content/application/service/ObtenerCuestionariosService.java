package com.lorenzomar3.AQ.content.application.service;

import com.lorenzomar3.AQ.content.application.port.in.ObtenerCuestionariosUseCase;
import com.lorenzomar3.AQ.content.application.port.out.TemarioRepositoryPort;
import com.lorenzomar3.AQ.content.domain.Temario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenerCuestionariosService implements ObtenerCuestionariosUseCase {

    private final TemarioRepositoryPort temarioRepositoryPort;

    public ObtenerCuestionariosService(TemarioRepositoryPort temarioRepositoryPort) {
        this.temarioRepositoryPort = temarioRepositoryPort;
    }

    @Override
    public List<Temario> obtenerCuestionarios() {
        return temarioRepositoryPort.findAllCuestionarios();
    }
}
