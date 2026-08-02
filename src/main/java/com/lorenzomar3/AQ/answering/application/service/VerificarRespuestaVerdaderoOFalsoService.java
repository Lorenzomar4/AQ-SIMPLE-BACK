package com.lorenzomar3.AQ.answering.application.service;

import com.lorenzomar3.AQ.answering.application.port.in.VerificarRespuestaVerdaderoOFalsoUseCase;
import com.lorenzomar3.AQ.answering.application.port.out.VerdaderoOFalsoParaResponderPort;
import com.lorenzomar3.AQ.answering.domain.VerdaderoOFalsoParaResponder;
import com.lorenzomar3.AQ.dto.newDto.RespuestaDePreguntaDTO;
import com.lorenzomar3.AQ.exception.BussinesException;
import org.springframework.stereotype.Service;

@Service
public class VerificarRespuestaVerdaderoOFalsoService implements VerificarRespuestaVerdaderoOFalsoUseCase {

    private final VerdaderoOFalsoParaResponderPort verdaderoOFalsoParaResponderPort;

    public VerificarRespuestaVerdaderoOFalsoService(VerdaderoOFalsoParaResponderPort verdaderoOFalsoParaResponderPort) {
        this.verdaderoOFalsoParaResponderPort = verdaderoOFalsoParaResponderPort;
    }

    @Override
    public Boolean verificar(RespuestaDePreguntaDTO respuesta) {
        VerdaderoOFalsoParaResponder verdaderoOFalsoParaResponder = verdaderoOFalsoParaResponderPort.findById(respuesta.idPregunta())
                .orElseThrow(() -> new BussinesException("No se encuentra una pregunta con el id solicitado"));

        Boolean esCorrecta = verdaderoOFalsoParaResponder.verificarRespuesta(respuesta.respuestaBooleana());
        verdaderoOFalsoParaResponderPort.save(verdaderoOFalsoParaResponder);
        return esCorrecta;
    }
}
