package com.lorenzomar3.AQ.content.application.port.in;

import com.lorenzomar3.AQ.content.domain.Temario;

import java.util.List;

public interface ObtenerCuestionariosUseCase {

    List<Temario> obtenerCuestionarios();
}
