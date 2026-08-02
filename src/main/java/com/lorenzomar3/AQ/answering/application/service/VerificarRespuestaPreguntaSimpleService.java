package com.lorenzomar3.AQ.answering.application.service;

import com.lorenzomar3.AQ.answering.application.port.in.VerificarRespuestaPreguntaSimpleUseCase;
import com.lorenzomar3.AQ.answering.application.port.out.PreguntaSimpleParaResponderPort;
import com.lorenzomar3.AQ.answering.domain.PreguntaSimpleParaResponder;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class VerificarRespuestaPreguntaSimpleService implements VerificarRespuestaPreguntaSimpleUseCase {

    private final PreguntaSimpleParaResponderPort preguntaSimpleParaResponderPort;

    public VerificarRespuestaPreguntaSimpleService(PreguntaSimpleParaResponderPort preguntaSimpleParaResponderPort) {
        this.preguntaSimpleParaResponderPort = preguntaSimpleParaResponderPort;
    }

    @Override
    public Boolean verificar(RespuestaDePreguntaDTO respuesta) {
        PreguntaSimpleParaResponder preguntaSimpleParaResponder = preguntaSimpleParaResponderPort.findById(respuesta.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        Boolean esCorrecta = preguntaSimpleParaResponder.verificarRespuesta(respuesta.respuestaBooleana());
        preguntaSimpleParaResponderPort.save(preguntaSimpleParaResponder);
        return esCorrecta;
    }
}
